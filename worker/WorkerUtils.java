import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class WorkerUtils 
{
	DatagramSocket socket;
	private boolean isUpdating = true;
	private boolean shutdown = false;
	private boolean debub = true;
	private InetAddress rmAddress;
	private int rmPort;
	Map<String, Integer> fileVersionMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	Map<String, Boolean> fileLockMap = Collections.synchronizedMap(new HashMap<String, Boolean>());

	public WorkerUtils(DatagramSocket socket)	
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

		isUpdating = false;
		System.out.println("Connected to RM");
	}

	public synchronized void incrementVersion(String filename)
	{
		if (fileVersionMap.get(filename) == null)
			fileVersionMap.put(filename, 0);
		else
			fileVersionMap.put(filename, fileVersionMap.get(filename) + 1);
	}

	public synchronized void updateFileVersion(String filename, int version)
	{
		fileVersionMap.put(filename, version);
	}

	public synchronized int getFileVersion(String filename)
	{
		return (fileVersionMap.get(filename) == null ? -1 : fileVersionMap.get(filename));
	}

	public synchronized boolean fileLockTaken(String fileName)
	{
		if (fileLockMap.get(fileName) == null)
			fileLockMap.put(fileName, false);
		return fileLockMap.get(fileName);
	}

	public synchronized void grabFileLock(String filename)
	{
		fileLockMap.put(filename, true);
	}

	public synchronized void returnFileLock(String filename)
	{
		fileLockMap.put(filename, false);
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

		if (debub)
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

			File f = new File("files/" + fileName);
			if (!f.exists()) 
			{
				sendPacket("__dne__", cAddress, cPort);
				System.out.println("\t" + "The file does not exit");
				f = null;
				returnFileLock(fileName);
				socket.close();
				return;
			}

			BufferedReader reader = Files.newBufferedReader(Paths.get("files/" + fileName));
		    String line = null;
		    
		    while ((line = reader.readLine()) != null) 
		    	sendPacket(line, cAddress, cPort);
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

		// Sent to client so they can know what address to send file to. content is meaningless
		packet = new DatagramPacket("".getBytes(), "".length(), cAddress, cPort);
		socket.send(packet);

		while(fileLockTaken(fileName))
			continue;

		grabFileLock(fileName);
			PrintWriter writer = new PrintWriter("files/" + fileName, "UTF-8");
			System.out.println("\t" + "Recieving...");

			String line;
			while ((line = getPacketAndDataAltSoc(socket)).compareTo("__end__") != 0)
			{
			    System.out.println("\t" + line);
			    writer.println(line);
			    packet = new DatagramPacket(line.getBytes(), line.length(), 
			    	cAddress, cPort);
			    socket.send(packet);
			}
			incrementVersion(fileName); 
		returnFileLock(fileName);

		writer.close();
		System.out.println("\t" + "File received succesfully");
		socket.close();
	}

	public synchronized void stuff()
	{

	}
}