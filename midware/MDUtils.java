import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;


@SuppressWarnings("serial")
public class MDUtils implements java.io.Serializable
{
	DatagramSocket socket;
	private LinkedList<RMInfo> rmList = new LinkedList<RMInfo>();
	Map<String, Integer> roleMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	// private LinkedList<WorkerInfo> workerList = new LinkedList<WorkerInfo>();

	public MDUtils(DatagramSocket socket)
	{
		this.socket = socket;
		initRoles();
	}

	// Using just one address w/ different ports
	public InetAddress getMCAddress() throws Exception
	{
		return InetAddress.getByName("228.66.77.88");
	}

	public String getPortForRM(int rmNum)
	{
		Map<Integer, String> rmPortMap = Collections.synchronizedMap(new HashMap<Integer, String>());
		rmPortMap.put(0,"65003,65004");
		rmPortMap.put(1,"65005,65006");
		rmPortMap.put(2,"65007,65008");

		// Using 3 for the communication between the actually RM
		rmPortMap.put(3,"65001,65002");

		if (rmNum == 4)
			return rmPortMap.get(3) + ',' + rmPortMap.get(rmList.size() - 1);
		return rmPortMap.get(rmNum);
	}

	public String getMCInfo(int rmNum) throws Exception
	{
		return getMCAddress() + "," + getPortForRM(rmNum);
	}

	public synchronized void pushRM(DatagramPacket packet) throws Exception
	{
		RMInfo rmInfo = new RMInfo(packet);
		rmList.push(rmInfo);
		setRole(0, getnextRole());
		sendPacket(getMCInfo(4), 0);
		if (rmList.size() == 1)
			sendPacket("__pass__", 0);
		else
			sendPacket(rmList.get(getRoleRep("upd")).getInfo(), 0);
	}

	public synchronized int getNumRM()
	{
		return rmList.size();
	}

	public synchronized boolean subFarmIsFull()
	{
		int totalW = 0;
		for (RMInfo rm: rmList)
			totalW += rm.getNumWorker();
		return totalW == 9;
	}

	public RMInfo getnextRM()
	{

		RMInfo lowRM = rmList.get(0);
		for (RMInfo rm: rmList) 
			if (rm.getNumWorker() < lowRM.getNumWorker())
				lowRM = rm;

		// int lowRM = 0;
		// for (int x=0; x<rmList.size(); x++) 
		// 	if (rmList.get(x).getNumWorker() < rmList.get(lowRM).getNumWorker())
		// 		lowRM = rm;
			
		return lowRM;
	}

	public RMInfo getRM(int rmNum)
	{
		return rmList.get(rmNum);
	}

	public RMInfo getRMFromAddress(InetAddress address)
	{
		RMInfo res = null;

		for (RMInfo rm: rmList)
			if (address == rm.getAddress())
				return rm;
		return res;
	}

	public int getRMNum(InetAddress address)
	{
		int x = 0;
		for (x=0; x< rmList.size(); x++)
			if (rmList.get(x).getAddress() == address)
				return x;
		return x;
	}

	public void initRoles()
	{
		roleMap.put("upl", 0);
		roleMap.put("upd", 0);
		roleMap.put("dwl", 0);
	}

	public void sendPacket(String data, int rmNum) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
			rmList.get(rmNum).getAddress(), rmList.get(rmNum).getPort());
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		socket.close();
	}

	public boolean exists(InetAddress address)
	{
		for (RMInfo rm: rmList)
			if (address == rm.getAddress())
				return true;
		return false;
	}

	public boolean isAlive(int rmNum) throws Exception
	{
		DatagramSocket pingSocket = new DatagramSocket();
		DatagramPacket pingPacket = new DatagramPacket("__ping__".getBytes(), "__ping__".length(),
			rmList.get(rmNum).getAddress(), rmList.get(rmNum).getPort());
		System.out.println("Pinging " + rmNum);
		pingSocket.send(pingPacket);
		byte[] rbuf = new byte[1024];
		pingPacket = new DatagramPacket(rbuf, rbuf.length);		
		pingSocket.setSoTimeout(10000);
		// pingSocket.receive(pingPacket);

		try {
				pingSocket.receive(pingPacket);
			} catch (SocketTimeoutException  e) {
				System.out.println("reached setSoTimeout");
				pingSocket.close();
				return false;
			}
		System.out.println("he's alive!!");
		pingSocket.close();
		return true;
	}

	public synchronized void addOldRM(InetAddress address) throws Exception
	{
		RMInfo rm = getRMFromAddress(address);
		rm.resurect();
		String role = getnextRole();
		int num = getRMNum(address);
		setRole(num, role);
		sendPacket(getMCInfo(4), num);
		sendPacket(rmList.get(getRoleRep("upd")).getInfo(), num);
		System.out.println("old host added back");
	}

	public int getAlive() throws Exception
	{
		// for (RMInfo rm: rmList)
		int x = 0;
		for (x=0; x< rmList.size(); x++)
			if(isAlive(x))
				return x;
		return x;
	}

	public void sendRolePacket(String data, String role) throws Exception
	{
		System.out.println("Sending " + data + " to " + role);

		if (!isAlive(getRoleRep(role)))
		{
			rmList.get(getRoleRep(role)).kill();
			setRole(getAlive(), role);
		}

		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
			rmList.get(getRoleRep(role)).getAddress(), rmList.get(getRoleRep(role)).getPort());
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		socket.close();
	}

	public void sendGenericPacket(String data, InetAddress address, int port) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), address, port);
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		socket.close();
	}

	public synchronized void setRole(int rmNum, String role)
	{
		roleMap.put(role, rmNum);
	}

	public synchronized int getRoleRep(String role)
	{
		return roleMap.get(role);
	}

	public synchronized String getnextRole()
	{
		if (roleMap.get("upl") == roleMap.get("dwl"))
			return "dwl";
		if (roleMap.get("upl") == roleMap.get("upd"))
			return "upl";
		if (roleMap.get("dwl") == roleMap.get("upd"))
			return "dwl";
		else
			return "dwl";
	}
}