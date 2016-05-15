import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.net.*;

public class WorkerUtils 
{
	DatagramSocket socket;
	private boolean isUpdating = true;
	private boolean shutdown = false;
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

	public void setUp()
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

		rmAddress = InetAddress.getByName(rmInfo.split(",")[0]);
		rmPort = rmInfo.split(",")[0];

		sendPacket("__server__", rmAddress, rmPort);

		isUpdating = false;
		System.out.println("Connected to RM");
	}

	public synchronized void incrementVersion(String filename)
	{
		if (fileVersionMap.get(fileName) == null)
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
		return fileVersionMap.get(filename) == null ? false : fileVersionMap.get(filename);
	}

	public synchronized boolean fileLockTaken(String fileName)
	{
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

	public synchronized void sendFile(String fileName, InetAddress address, int port) throws Exception
	{
		while(fileLockTaken(fileName))
			continue;
		
		grabFileLock(fileName);
			System.out.println("sending file to client...");

			File f = new File(fileName);
			if (!f.exists()) 
			{
				sendPacket("__dne__", address, port);
				System.out.println("The file does not exit");
				f = null;
				returnFileLock(fileName);
				return;
			}

			BufferedReader reader = Files.newBufferedReader(Paths.get(socket.getLocalPort() +'/' + fileName), "UTF-8"));
		    String line = null;
		    
		    while ((line = reader.readLine()) != null) 
		    	sendPacket(line, address, port);
		    sendPacket("__end__", address, port);
		returnFileLock(fileName);
	    
		System.out.println("File sent succesfully");
	}

	public synchronized void recieveFile(DatagramPacket packet) throws Exception
	{
		String line;
		String uploadInfo = getDataFromPacket(packet);
		String fileName = uploadInfo.split(",")[1];

		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket();
		InetAddress cAddress = InetAddress.getByName(uploadInfo.split(",")[2]);
		
		int cPort = Integer.parseInt(uploadInfo.split(",")[3]);

		// Sent to host so they can know what address to send file to. content is meaningless
		socket.send(''.getBytes(), ''.getLength(), cAddress, cPort);

		while(fileLockTaken(fileName))
			continue;

		grabFileLock(fileName);
			PrintWriter writer = new PrintWriter("files/" + fileName, "UTF-8");
			System.out.println("Recieving...");

			while ((line = getPacketAndData()).compareTo("__end__") !=0)
			{
			    System.out.println(line);
			    writer.println(line);
			    packet = new DatagramPacket(line.getBytes(), line.length(), 
			    	cAddress, cPort);
			    socket.send(packet);
			}
			incrementVersion(fileName); 
		returnFileLock(fileName);

		writer.close();
		System.out.println("File received succesfully");
	}

	public synchronized void 
	{

	}
}