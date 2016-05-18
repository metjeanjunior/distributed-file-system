import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class WorkerUtils 
{
	DatagramSocket socket;

	// used to prevent overwriting during mc update
	private boolean selfUpload = false;
	private boolean isUpdating = true;
	private boolean shutdown = false;
	private boolean debug = true;

	private InetAddress rmAddress;
	private int rmPort;

	Map<String, Integer> fileVersionMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	Map<String, Boolean> fileLockMap = Collections.synchronizedMap(new HashMap<String, Boolean>());

	MulticastSocket updateSocket; 
	MulticastSocket uploadSocket;

	InetAddress group;
	int updatePort;
	int uploadPort;

	public WorkerUtils(DatagramSocket socket)	
	{
		this.socket = socket;
	}

	public boolean isUpdating()
	{
		return isUpdating;
	}

	public String getDirName()
	{
		return socket.getLocalPort() + "/";
	}

	public boolean isShutDown()
	{
		return shutdown;
	}

	public void setUp() throws Exception
	{
		isUpdating = true;

		String rmInfo = getPacketAndData();

		if (rmInfo.compareTo("__quit__") == 0)
		{
			shutdown = true;
			isUpdating = false;
			System.out.println("Connection to farm was rejected. Try again later.");
			return;
		}
		
		rmAddress = InetAddress.getByName(rmInfo.split(",")[0].substring(1));
		rmPort = Integer.parseInt(rmInfo.split(",")[1]);

		sendPacket("__server__", rmAddress, rmPort);
		String mcInfo = getPacketAndData();
		
		if (debug)
			System.out.println("Setting up with:" + mcInfo);

		group = InetAddress.getByName(mcInfo.split(",")[0].substring(1));
		uploadPort = Integer.parseInt(mcInfo.split(",")[1]);
		updatePort = Integer.parseInt(mcInfo.split(",")[2]);

		updateSocket = new MulticastSocket(updatePort);
		uploadSocket = new MulticastSocket(uploadPort);

		// System.out.println("joining mc on group: " + group);
		updateSocket.joinGroup(group);
		uploadSocket.joinGroup(group);

		isUpdating = false;
		System.out.println("Connected to RM");
	}

	public synchronized void incrementVersion(String fileName)
	{
		if (fileVersionMap.get(fileName) == null)
			fileVersionMap.put(fileName, 0);
		else
			fileVersionMap.put(fileName, fileVersionMap.get(fileName) + 1);
	}

	public synchronized void updateFileVersion(String fileName, int version)
	{
		fileVersionMap.put(fileName, version);
	}

	public synchronized int getFileVersion(String fileName)
	{
		return (fileVersionMap.get(fileName) == null ? -1 : fileVersionMap.get(fileName));
	}

	public synchronized boolean fileLockTaken(String fileName)
	{
		if (fileLockMap.get(fileName) == null)
			fileLockMap.put(fileName, false);
		return fileLockMap.get(fileName);
	}

	public synchronized void grabFileLock(String fileName)
	{
		fileLockMap.put(fileName, true);
	}

	public synchronized void returnFileLock(String fileName)
	{
		fileLockMap.put(fileName, false);
	}


	public synchronized void sendPacket(String data, InetAddress address, int port) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), address, port);
		socket.send(packet);
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

		if (debug)
			System.out.println("\t" + "Recieving from: " + packet.getPort());
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

	// public synchronized void sendFile(String fileName, InetAddress address, int port) throws Exception
	public synchronized void sendFile(DatagramPacket packet) throws Exception
	{
		DatagramSocket socket = new DatagramSocket();

		String uploadInfo = getDataFromPacket(packet);
		String fileName = uploadInfo.split(",")[1];

		InetAddress cAddress = InetAddress.getByName(uploadInfo.split(",")[2].substring(1));
		int cPort = Integer.parseInt(uploadInfo.split(",")[3]);

		// Sent to client so they can know what address to send file to. content is meaningless
		packet = new DatagramPacket("".getBytes(), "".length(), cAddress, cPort);
		socket.send(packet);

		while(fileLockTaken(fileName))
			continue;
		
		grabFileLock(fileName);
			System.out.println("\t" + "sending file to client...");

			// File f = new File("files/" + fileName);
			File f = new File(getDirName() + fileName);

			if (!f.exists()) 
			{
				sendPacket("__dne__", cAddress, cPort);
				System.out.println("\t" + "The file does not exit");
				f = null;
				returnFileLock(fileName);
				socket.close();
				return;
			}

			// BufferedReader reader = Files.newBufferedReader(Paths.get("files/" + fileName));
			BufferedReader reader = Files.newBufferedReader(Paths.get(getDirName() + fileName));

		    String line = null;
		    
		    while ((line = reader.readLine()) != null)
		    {
		    	System.out.println(line);
		    	sendPacket(line, cAddress, cPort);
		    }
		    sendPacket("__end__", cAddress, cPort);
		returnFileLock(fileName);
	    
		System.out.println("\t" + "File sent succesfully");
		socket.close();
	}

	public synchronized void recieveFile(DatagramPacket packet) throws Exception
	{
		DatagramSocket socket = new DatagramSocket();

		String uploadInfo = getDataFromPacket(packet);
		String fileName = uploadInfo.split(",")[1];

		InetAddress cAddress = InetAddress.getByName(uploadInfo.split(",")[2].substring(1));
		int cPort = Integer.parseInt(uploadInfo.split(",")[3]);

		InetAddress rmAddress = packet.getAddress();
		int rmPort = packet.getPort();

		// Sent to client so they can know what address to send file to. content is meaningless
		packet = new DatagramPacket("".getBytes(), "".length(), cAddress, cPort);
		socket.send(packet);

		while(fileLockTaken(fileName))
			continue;

		selfUpload = true;

		String fileInfo = fileName + ',' + getFileVersion(fileName);
		sendMCUpload(fileInfo);
		sendPacket(fileInfo, rmAddress, rmPort);

		grabFileLock(fileName);
			// PrintWriter writer = new PrintWriter("files/" + fileName, "UTF-8");
			PrintWriter writer = new PrintWriter(getDirName() + fileName, "UTF-8");
			System.out.println("saving in " + getDirName() + fileName);

			System.out.println("\t" + "Recieving...");

			String line;
			while (true)
			{
			    // System.out.println("\t" + line);
				line = getPacketAndDataAltSoc(socket);

				if (line == null)
					line = "";
				if (line.compareTo("__end__") == 0)
					break;
			    writer.println(line);
			    sendMCUpload(line);
			    sendPacket(line, rmAddress, rmPort);
			}
			sendMCUpload("__end__");
			sendPacket("__end__", rmAddress, rmPort); 
			incrementVersion(fileName); 
		returnFileLock(fileName);

		writer.close();
		selfUpload = false;
		System.out.println("\t" + "File received succesfully");
		socket.close();
	}

	public synchronized boolean selfUpload()
	{
		return selfUpload;
	}

	public  InetAddress getGroup()
	{
		return group;
	}

	public  int getUpdatePort()
	{
		return updatePort;
	}

	public  int getUploadPort()
	{
		return uploadPort;
	}

	public synchronized void sendMCUpload(String data) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), group, uploadPort);
		uploadSocket.send(packet);
		// System.out.println("Sending to " + group + ":" + uploadPort);
		// System.out.println("\t\t\t\t\t\t"+data);
	}

	public synchronized void update(DatagramPacket packet) throws Exception
	{
		InetAddress address = packet.getAddress();
		int port = packet.getPort();
		DatagramSocket socket = new DatagramSocket();

		File directory = new File(getDirName());
		File[] fList = directory.listFiles();
		System.out.println("About to upadte " + fList.length + " files from " + getDirName());
		
		packet = new DatagramPacket("".getBytes(), "".length(), packet.getAddress(), packet.getPort());
		socket.send(packet);

		for (File file : fList)
		{
			String fileName = file.getName();
			String fileInfo = file.getName() + "," + getFileVersion(file.getName());
			System.out.println("Sending " + fileInfo);
			packet = new DatagramPacket(fileInfo.getBytes(), fileInfo.length(), address, port);
        	socket.send(packet);

        	String need = getPacketAndDataAltSoc(socket);
			if (need.compareTo("__pass__") == 0)
			{
				System.out.println("File not needed");
				continue;
			}
			else
				System.out.println("the file is needed and being sent");

        	BufferedReader reader = Files.newBufferedReader(Paths.get(getDirName() + fileName));

		    String line = null;
		    
		    while ((line = reader.readLine()) != null)
	    	{ 
	    		System.out.println("Sending: " + line);
				packet = new DatagramPacket(line.getBytes(), line.length(), address, port);
	        	socket.send(packet);
		    }
			packet = new DatagramPacket("__end__".getBytes(), "__end__".length(), address, port);
        	socket.send(packet);
        	System.out.println("\tfinished sending prev file.");
		}
		packet = new DatagramPacket("__done__".getBytes(), "__done__".length(), address, port);
    	socket.send(packet);

		socket.close();
	}

	public void sendFileVer(DatagramPacket packet) throws Exception
	{
		System.out.println("proccessing file need request");
		DatagramSocket socket = new DatagramSocket();
		packet = new DatagramPacket("".getBytes(), "".length(), packet.getAddress(), packet.getPort());
		socket.send(packet);
		String fileInfo = getPacketAndDataAltSoc(socket);
		System.out.println("Request came in for "+ fileInfo);

		int fVer = Integer.parseInt(fileInfo.split(",")[1]);

		if(getFileVersion(fileInfo.split(",")[0]) == fVer)
			sendPacket("no", packet.getAddress(), packet.getPort());
		else
			sendPacket("yes", packet.getAddress(), packet.getPort());
	}
}