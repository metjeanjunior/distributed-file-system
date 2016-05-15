import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class RMUtils implements java.io.Serializable
{
	private DatagramSocket socket;
	protected LinkedList<WorkerInfo> workerList = new LinkedList<WorkerInfo>();
	Map<String, Integer> roleMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	private boolean isUpdating = true;
	private boolean shutdown = false;

	MulticastSocket updateSocket; 
	MulticastSocket uploadSocket; 
	
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

	public void setUp() throws Exception
	{
		isUpdating = true;

		String mcInfo = getPacketAndData();

		if (mcInfo.compareTo("__quit__") == 0)
		{
			shutdown = true;
			isUpdating = false;
			System.out.println("Farm is already full. Try again later.");
			return;
		}

		System.out.println("Connected to MD");

		InetAddress rmGroup = InetAddress.getByName(mcInfo.split(",")[0]);
		int updatePort = Integer.parseInt(mcInfo.split(",")[1]);
		int uploadPort = Integer.parseInt(mcInfo.split(",")[2]);

		updateSocket = new MulticastSocket(updatePort);
		uploadSocket = new MulticastSocket(uploadPort);
		updateSocket.joinGroup(rmGroup);
		updateSocket.joinGroup(rmGroup);
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
		genPacket = new DatagramPacket(data.getBytes(), data.length(), genPacket.getAddress(), genPacket.getPort());
		socket.send(genPacket);
	}

	@SuppressWarnings("deprecation")
	public String getDataFromPacket(DatagramPacket packet) throws Exception
	{
		ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
		DataInputStream dis = new DataInputStream(bin);

		return dis.readLine();
	}

	public synchronized String getPacketAndData() throws Exception
	{
		byte[] rbuf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);		
		socket.receive(packet);

		return getDataFromPacket(packet);
	}

	// Same as above but recieves from specific socket as opposed to main socket
	// Get packet and data from an alternate socket (meaning of name)
	public synchronized String getPacketAndDataAltSoc(DatagramSocket socket) throws Exception
	{
		byte[] rbuf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);		
		socket.receive(packet);

		return getDataFromPacket(packet);
	}

	public synchronized DatagramPacket getPacket() throws Exception
	{
		byte[] rbuf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);		
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

	// On steriods as it also manages updating RMs thru new socket
	// We cant be wasteful :)
	public void sendRolePacket(String data, String role) throws Exception
	{
		int pass = 0;
		if(role.compareTo("upl+") == 0)
		{
			pass = 1;
			role = "upl";
		}

		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
				workerList.get(getRoleRep(role)).getAddress(), workerList.get(getRoleRep(role)).getPort());
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		uploadMC(socket);
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
		if (roleMap.get("upl") == roleMap.get("dwl"))
			return "dwl";
		if (roleMap.get("upl") == roleMap.get("upd"))
			return "upl";
		// if (roleMap.get("dwl") == roleMap.get("upd"))
		else
			return "dwl";
	}

	public InetAddress getWorkerAddress(int workerNum)
	{
		return workerList.get(workerNum).getAddress();
	}

	public int getWorkerPort(int workerNum)
	{
		return workerList.get(workerNum).getPort();
	}

	public void uploadMC(DatagramSocket socket) throws Exception
	{
		System.out.println("uploading prev recieved file to other farms...");
		String line;
		while ((line = getPacketAndDataAltSoc(socket)).compareTo("__end__") != 0)
		{
		    System.out.println(line);
		 	DatagramPacket packet = new DatagramPacket(line.getBytes(), line.length(), 
		 		socket.getLocalPort(), socket.getLocalSocketAddress());   
		 	socket.send(packet);
		}
	}

	public void updateMC(DatagramPacket socket)
	{
		
	}
}