import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class WorkerUtils 
{
	DatagramSocket socket;
	private boolean isUpdating = true;
	Map<String, Integer> fileVersionMap = Collections.synchronizedMap(new HashMap<String, Integer>());

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
		return fileVersionMap.get(filename);
	}

	public void sendFile(String fileName, InetAddress address, int port) throws Exception
	{
		Charset charset = Charset.forName("US-ASCII");
		clientAddress = address;
		clientPort = port;
		System.out.println("sending file to client...");

		File f = new File(fileName);
		if (!f.exists()) 
		{
			sendPacket("__dne__");
			System.out.println("The file does not exit");
			f = null;
			return;
		}

		try (BufferedReader reader = Files.newBufferedReader(Paths.get(socket.getLocalPort() +'/' + fileName), charset)) {
		    String line = null;
		    while ((line = reader.readLine()) != null) 
		    	sendPacket(line);
		    sendPacket("__end__");
		    
			System.out.println("File sent succesfully");
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}

	public void recieveFile(String fileName) throws Exception
	{
		// Charset charset = Charset.forName("US-ASCII");
		String line;
		Semaphore mutex = new Semaphore(1);
		myself = true;
		sendMCLine("__newFile__," + fileName);

		try (PrintWriter writer = new PrintWriter(socket.getLocalPort() +'/' + fileName, "UTF-8"))
		{
			System.out.println("Recieving...");
			mutex.acquire();
			while ((line = receivePacketAndData()).compareTo("__end__") !=0)
			{
			    System.out.println(line);
			    writer.println(line);
			    sendMCLine(line);
			}
			mutex.release();

			writer.close();
			System.out.println("File received succesfully");
		} 
		catch (IOException x) 
		{
		    System.err.format("IOException: %s%n", x);
		}
		myself = false;
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