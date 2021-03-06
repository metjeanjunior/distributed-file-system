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
	private boolean selfUploading = false;
	boolean upd = false;
	boolean workerFull = false;

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
		System.out.println("Connected to MD. Setting up...");
		String mcInfo = getPacketAndData();
			System.out.println("Setting up with:" + mcInfo);

		String updateInfo = getPacketAndData();
			System.out.println("\t" + updateInfo);

		if (debug)
		{
			System.out.println("Setting up with:" + mcInfo);
			System.out.println("\t" + updateInfo);
		}

		if (mcInfo.compareTo("__quit__") == 0)
		{
			shutdown = true;
			isUpdating = false;
			System.out.println("Farm is already full. Try again later.");
			return;
		}

		System.out.println("Connected to MD");

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

		// String updateInfo = getPacketAndData();

		if (updateInfo.compareTo("__pass__") != 0)
		{
			System.out.println("waiting for workers to connect");
			// if (!workerFull)
				for (int x=0; x<2; x++)
				{
					byte[] rbuf = new byte[1024];
					DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
					socket.receive(packet);
					pushWorker(packet);
					String req = getDataFromPacket(packet);
				}
			System.out.println("About to update");
			update(updateInfo);
		}

		if (debug)
			System.out.println("Done setting up");
		isUpdating = false;
	}

	@SuppressWarnings("deprecation")
	public void update(String updateInfo) throws Exception
	{
		System.out.println("updating...");
		DatagramSocket updateS = new DatagramSocket();
		InetAddress address = InetAddress.getByName(updateInfo.split(":")[0].substring(1));
		int port = Integer.parseInt(updateInfo.split(":")[1]);

		DatagramPacket packet  = new DatagramPacket("__update__".getBytes(), "__update__".length(), 
			address, port);
		updateS.send(packet);
		updateS.receive(packet);
    	// System.out.println(String(packet.getData(), packet.getLength()));
	    String tell = getDataFromPacket(packet);
	    System.out.println("told... " + tell);
	    if (tell.compareTo("__quit__") == 0)
	    {
	    	System.out.println("The directory is currently empty");
	    	return;
	    }
		address = packet.getAddress();
		port = packet.getPort();

		String line;
		while ((line = getPacketAndDataAltSoc(updateS)).compareTo("__done__") != 0)
		{

			System.out.println("Sending file: " + line);
			if(!needsFile(line))
			{
				System.out.println("file not needed");
				packet  = new DatagramPacket("no".getBytes(), "no".length(), 
					address, port);
				updateS.send(packet);
				continue;
			}
			else
			{
				System.out.println("file is needed");
				packet  = new DatagramPacket("yes".getBytes(), "yes".length(), 
					address, port);
				updateS.send(packet);
			}

			sendToWMC(line);
			while (true)
			{
				line = getPacketAndDataAltSoc(updateS);
				if (line == null)
						line = "";
				if (line.compareTo("__end__") == 0)
					break;
				System.out.println("Sending line: " + line);
				sendToWMC(line);
			}
			sendToWMC("__end__");
		}
		sendToWMC("__done__");

		System.out.println("finished updating directory");
		updateS.close();
	}

	public synchronized void serveUpd(DatagramPacket packet) throws Exception
	{
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = packet.getAddress();
		int port = packet.getPort();
    
	    if(!upd)
	    {
		    packet  = new DatagramPacket("__quit__".getBytes(), "__quit__".length(), 
		    	address, port);
		    System.out.println("Update rejected as no files up");
			socket.send(packet);
	    	return;
	    }

		packet  = new DatagramPacket("__update__".getBytes(), "__update__".length(), address, port);
		socket.send(packet);

		System.out.println("Connected to upd worker");
		int roleRep = getRoleRep("upd");
		InetAddress wAddress = workerList.get(roleRep).getAddress();
		int wPort = workerList.get(roleRep).getPort();
		packet = new DatagramPacket("__update__".getBytes(), "__update__".length(),
			wAddress, wPort);
		socket.send(packet);

		socket.receive(packet);
		wAddress = packet.getAddress();
		wPort = packet.getPort();

		String line;
		while ((line = getPacketAndDataAltSoc(socket)).compareTo("__done__") != 0)
		{
			packet = new DatagramPacket(line.getBytes(), line.length(), address, port);
			System.out.println("Sending file: " + line);
			socket.send(packet);

			String need = getPacketAndDataAltSoc(socket);
			System.out.println("the need is " + need);
			if (need.compareTo("no") == 0)
			{
				packet = new DatagramPacket("__pass__".getBytes(), "__pass__".length(), wAddress, wPort);
				socket.send(packet);
				System.out.println("This file is needed");
				continue;
			}
			else
			{
				packet = new DatagramPacket("yes".getBytes(), "yes".length(), wAddress, wPort);
				socket.send(packet);
				System.out.println("This file is not needed");
			}

			while (true)
			{
				line = getPacketAndDataAltSoc(socket);

				if (line == null)
					line = "";
				if (line.compareTo("__end__") == 0)
					break;

				System.out.println("Sending line: " + line);
				packet = new DatagramPacket(line.getBytes(), line.length(), address, port);
				socket.send(packet);
			}
			packet = new DatagramPacket("__end__".getBytes(), "__end__".length(), address, port);
			socket.send(packet);
		}
		packet = new DatagramPacket("__done__".getBytes(), "__done__".length(), address, port);
		socket.send(packet);

		System.out.println("Finished servicing update");
		socket.close();
	}

	public synchronized  void sendPacket(String data, int workerNum) throws Exception
	{
		DatagramSocket socket = new DatagramSocket();
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
		workerFull = true;	
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
		    upd = true;
			selfUploading = true;
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
		selfUploading = true;
		upd = true;
		while (true)
		{
			line = getPacketAndDataAltSoc(socket);
			if (line == null)
					line = "";
			if (line.compareTo("__end__") == 0)
				break;
		 	sendToRMC(line);
		}
		sendToRMC("__end__");
		socket.close();
		selfUploading = false;
	}

	public synchronized void sendToRMC(String data) throws Exception
	{
		InetAddress address = InetAddress.getByName(group.substring(1));
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), address, uploadPort);
		uploadSocket.send(packet);
		System.out.println("Sending to " + group + ":" + uploadPort);
		System.out.println("\t"+data);
	}	

	public synchronized void sendToWMC(String data) throws Exception
	{
		InetAddress address = InetAddress.getByName(group.substring(1));
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), address, wUploadPort);
		wUploadSocket.send(packet);
		System.out.println("Sending to " + group + ":" + wUploadPort);
		System.out.println("\t"+data);
	}

	public boolean needsFile(String fileName) throws Exception
	{
		int worker = getRoleRep("upd");
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket packet = new DatagramPacket("__rqfv__".getBytes(), "__rqfv__".length(), getWorkerAddress(worker), getWorkerPort(worker));
		socket.send(packet);
		socket.receive(packet);
		System.out.println("Client network data recieved");
		packet = new  DatagramPacket(fileName.getBytes(), fileName.length(), packet.getAddress(), packet.getPort());
		socket.send(packet);

		String need = getPacketAndDataAltSoc(socket);
		System.out.println("the need replies " + need);
		return need.compareTo("yes") == 0;
	}

	public synchronized void recieveRFile()
	{

	}

	public boolean selfUploading()
	{
		return selfUploading;
	}

	public synchronized void flagSeflUpload()
	{
		selfUploading = true;
	}
}