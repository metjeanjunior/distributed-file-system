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
	MulticastSocket wUpdateSocket; 
	MulticastSocket wUploadSocket;
	
	String group;
	int updatePort;
	int uploadPort;
	int wUpdatePort; 
	int wUploadPort;
	private boolean debug = true; 
	
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

		if (debug)
			System.out.println("Setting up with:" + mcInfo);

		group = mcInfo.split(",")[0];
		InetAddress groupInet = InetAddress.getByName(group.substring(1));
		updatePort = Integer.parseInt(mcInfo.split(",")[1]);
		uploadPort = Integer.parseInt(mcInfo.split(",")[2]);
		wUpdatePort = Integer.parseInt(mcInfo.split(",")[3]);
		wUploadPort = Integer.parseInt(mcInfo.split(",")[4]);

		updateSocket = new MulticastSocket(updatePort);
		uploadSocket = new MulticastSocket(uploadPort);

		wUpdateSocket = new MulticastSocket(wUpdatePort);
		wUploadSocket = new MulticastSocket(wUploadPort);

		updateSocket.joinGroup(groupInet);
		uploadSocket.joinGroup(groupInet);

		wUpdateSocket.joinGroup(groupInet);
		wUploadSocket.joinGroup(groupInet);
		
		initRoles();

		if (debug)
			System.out.println("Done setting up");
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

	public synchronized void pushWorker(DatagramPacket packet) throws Exception
	{
		WorkerInfo workerInfo = new WorkerInfo(packet);
		workerList.push(workerInfo);
		setRole(0, getnextRole());
		sendPacket(getWMCInfo(), 0);
		System.out.println("\t" + "Just added a new worker to subfarm.");
		
	}

	public String getRMMCInfo()
	{
		return group + ',' + updatePort + ',' + uploadPort;	
	}

	public String getWMCInfo()
	{
		return group + ',' + wUpdatePort + ',' + wUploadPort;
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

		// Used to mark that upl is for file (overloaded)
		if(role.compareTo("upl+") == 0)
		{
			pass = 1;
			role = "upl";
		}

		System.out.println("\t" + "Sending " + data + " to " + role);

		InetAddress address = workerList.get(getRoleRep(role)).getAddress();
		int port = workerList.get(getRoleRep(role)).getPort();
		System.out.println("data being sent to " + address + ":" + port);

		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
				address, port);
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);

		if (pass == 1)
			uploadMC(socket);
		// socket.close();
	}

	public synchronized void setRole(int rmNum, String role)
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
		System.out.println("\t" + "uploading prev recieved file to other farms...");
		String line;
		InetAddress address = InetAddress.getByName(group.substring(1));
		while ((line = getPacketAndDataAltSoc(socket)).compareTo("__end__") != 0)
		{
		    System.out.println("\t" + line);
		 	DatagramPacket packet = new DatagramPacket(line.getBytes(), line.length(), address, uploadPort);
		 	uploadSocket.send(packet);
		}
		socket.close();
	}

	public void updateMC(DatagramPacket socket)
	{
		
	}
}