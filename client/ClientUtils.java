import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ClientUtils
{
	private InetAddress hostAddress;
	private int hostPort;
	private boolean shutdown = false;
	DatagramSocket socket;
	private boolean debug = true;
	
	public ClientUtils(DatagramSocket socket)
	{
		this.socket = socket;
	}

	public String getRequestInfo() throws Exception
	{
		System.out.println("Welcome to the Beast File Sharing System");
		System.out.println("Please type your request in the following format: upload/download filename");
		System.out.print(">>> ");

		BufferedReader keyboard = new BufferedReader( new InputStreamReader( System.in ) );
		String request = keyboard.readLine();

		String fileType = request.split(" ")[0];
		String fileName = request.split(" ")[1];

		while (!(fileType.toLowerCase().compareTo("upload") == 0) && !(fileType.toLowerCase().compareTo("download") == 0))
		 {
			System.out.println("Type upload/download");
			System.out.print(">>> ");
			fileType =  keyboard.readLine().toLowerCase();
		}

		if (fileType.compareTo("download") == 0)
			return request;
			
		File f = new File("files/" + fileName);
		while (!f.exists()) 
		{
			fileName = getNewFileName(fileName);

			if (fileName.compareTo("__quit__") == 0)
			{
				exit();
				return fileName;
			}

			f = new File("files/" + fileName);

		}
		
		return request;
	}

	public String getNewFileName(String fileName) throws Exception
	{
		System.out.println("You requested the following file: " + fileName);
		System.out.println("We could not find this file.");
		System.out.println("Would you like to change the file name?");

		BufferedReader keyboard = new BufferedReader( new InputStreamReader( System.in ) );
		String response = "";

		while (!(response.compareTo("yes") == 0) && !(response.compareTo("no") == 0))
		{
			System.out.println("Type yes/no");
			System.out.print(">>> ");

			response =  keyboard.readLine().toLowerCase();
		}

		if (response.compareTo("yes") == 0)
		{
			System.out.print("Please type the name of the file >>> ");
			keyboard = new BufferedReader( new InputStreamReader( System.in ) );
			response =  keyboard.readLine();
			return response;
		}
		else 
		{
			return "__quit__";
		}
	}

	public void setHostInfo1(DatagramPacket packet) throws Exception
	{
		hostAddress = packet.getAddress();
		hostPort = packet.getPort();
	}

	@SuppressWarnings("deprecation")
	public void setHostInfo(DatagramPacket packet) throws Exception
	{
		String data = getDataFromPacket(packet);
		
		if (debug)
			System.out.println("Recieved " + data);

		if (data != null && data.compareTo("__quit__") == 0)
		{
			System.out.println("Servers are currently not up :( Sorry");
			shutdown = true;
			return;
		}
			
		// String[] data1 = data.split(",");

		// hostAddress = InetAddress.getByName(data1[0].substring(1));
		// hostPort = Integer.parseInt(data1[1]);

		hostAddress = packet.getAddress();
		hostPort = packet.getPort();
	}

	public void sendLine(String data) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), hostAddress, hostPort);
		socket.send(packet);
	}

	public void uploadFile(String fileName) throws Exception
	{
		System.out.println("uploading to " + hostAddress + " with port " + hostPort + "...");

		// String fileInfo = "__filename__," +fileName;
		// sendLine(fileInfo);

		BufferedReader reader = Files.newBufferedReader(Paths.get("files/" +fileName));
	    String line = null;

	    while ((line = reader.readLine()) != null) 
	    {
	        // System.out.println(line);
	    	sendLine(line);
	    }

	    sendLine("__end__");
		System.out.println("File upload was succesfull");
	}

	public void downloadFile(String fileName) throws Exception
	{
		System.out.println("Waiting for file from Server..." + fileName);
		String line = null;

		PrintWriter writer = new PrintWriter("files/" +fileName, "UTF-8");

		while (true)
		{
			line = getPacketAndData();
			if (line == null)
					line = "\n";
			if (line.compareTo("__end__") == 0)
				break;
				
			if (line.compareTo("__dne__") ==0)
			{
			    System.out.println("The file you indicated is not hosted in on our servers");
			    System.out.println("Please try again with a different name");
			    Files.deleteIfExists(Paths.get(fileName));
			    exit();
			    return;
			}
			
			System.out.println(line);
		    writer.println(line);
		    // System.out.println("download doesnt work. fix!!!!!!");
		}
		writer.close();
		System.out.println("File download was succesfull");
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

	public boolean isShutDown()
	{
		return shutdown;
	}

	public void exit()
	{
		System.out.println("Thanks for using the Beast File Sharing System");
		System.out.println("Your request was submitted and processed succefully");
		// socket.close();
	}
}