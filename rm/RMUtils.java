import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

public class RMUtils implements java.io.Serializable
{
	private DatagramSocket socket;
	protected LinkedList<WorkerInfo> workerList = new LinkedList<WorkerInfo>();
	Map<String, Integer> roleMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	private boolean isUpdating = true;
	private boolean shutdown = false;

	MulticastSocket updateDirSocket; 
	
	public RMUtils(DatagramSocket socket)
	{
		this.socket = socket;
	}

	public boolean isUpdating()
	{
		return isUpdating;
	}

	public boolean isShutDown()
	{
		return shutdown;
	}

	public void setUp()
	{
		isUpdating = true;

		String updateDirInfo = getPacketAndData();

		if (updateDirInfo.compareTo("__quit__") == 0)
		{
			shutdown = true;
			isUpdating = false;
			System.out.println("Farm is already full. Try again later.");
			return;
		}

		System.out.println("Connected to MD");

		InetAddress updateDirGroup = InetAddress.getByName(updateDirInfo.split(",")[0]);
		int updateDirPort = updateDirInfo.split(",")[1];
		int pos = Integer.parseInt(updateDirInfo.split(",")[2]);

		updateDirSocket = new MulticastSocket(updateDirPort);
		updateDirSocket.joinGroup(updateDirGroup);
		isUpdating = false;
	}

	public synchronized  void sendPacket(String data, int workerNum) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
			getWorkerAddress(workerNum), getWorkerPort(workerNum));
		socket.send(packet);
	}

	public synchronized void sendGenericPacket(String data, DatagramPacket genPacket) throws Exception
	{
		if (data.length() == 0)
		{
			socket.send(packet);
			return;
		}
		
		genPacket = new DatagramPacket(data.getBytes(), data.length(), genPacket.getAddress(), genPacket.getPort());
		socket.send(genPacket);
	}

	@SuppressWarnings("deprecation")
	public String getDataFromPacket(DatagramPacket packet) throws Exception
	{
		bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
		dis = new DataInputStream(bin);

		return dis.readLine();
	}

	public synchronized String getPacketAndData() throws Exception
	{
		byte[] rbuf = new byte[1024];
		packet = new DatagramPacket(rbuf, rbuf.length);		
		socket.receive(packet);

		return getDataFromPacket(packet);
	}

	public synchronized DatagramPacket getPacket() throws Exception
	{
		byte[] rbuf = new byte[1024];
		packet = new DatagramPacket(rbuf, rbuf.length);		
		socket.receive(packet);
		return packet;
	}

	public synchronized void pushWorker(DatagramPacket packet)
	{
		WorkerInfo workerInfo = new WorkerInfo(packet);
		workerList.push(workerInfo);
		// primary = primary == null ? workerInfo : primary;
		// primaryNum = primaryNum == -1 ? workerList.size()-1 :primaryNum;
	}

	public void initRoles()
	{
		roleMap.put("upl", 0);
		roleMap.put("upd", 0);
		roleMap.put("dwl", 0);
	}

	public void sendRolePacket(String data, String role)
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
			rmList.get(getRoleRep(role).getAddress(), rmList.get(getRoleRep(role)).getPort());
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		socket.close();
	}

	public synchronized void setRole(String role, int rmNum)
	{
		roleMap.put(role, rmNum);
	}

	public synchronized int getRoleRep(String role)
	{
		return roleMap.get(role);
	}

	public synchronized String nextRole()
	{
		String role;

		if (roleMap.get("upl") == roleMap.get("dwl"))
			return "dwl";
		if (roleMap.get("upl") == roleMap.get("upd"))
			return "upl";
		if (roleMap.get("dwl") == roleMap.get("upd"))
			return "dwl";

		return role;
	}
}