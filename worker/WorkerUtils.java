import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class WorkerUtils 
{
	DatagramSocket socket;
	private boolean isUpdating = true;
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

	public void setUp()
	{
		isUpdating = true;

		
		
		isUpdating = false;
	}

	public synchronized void incrementVersion(String filename)
	{
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

	public synchronized void recieveFile(String fileName) throws Exception
	{
		String line;

		while(fileLockTaken(fileName))
			continue;

		grabFileLock(fileName);
			PrintWriter writer = new PrintWriter("files/" + fileName, "UTF-8")
			System.out.println("Recieving...");

			while ((line = receivePacketAndData()).compareTo("__end__") !=0)
			{
			    System.out.println(line);
			    writer.println(line);
			}
		returnFileLock(fileName);

		writer.close();
		System.out.println("File received succesfully");
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

	public String getDownloader()
	{

	}

	public void sendToDownloader()
	{
		
	}

	// Returns the server that facilitates the upload request from the client
	public String getUploadder()
	{

	}

	public void sendToUploader()
	{

	}

	// Returns the server that facilitates the update of the directory
	public String getUpdater()
	{

	}
}