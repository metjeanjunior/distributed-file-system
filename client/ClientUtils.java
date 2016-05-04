public class ClientUtils
{
	private InetAddress hostAddress;
	private int hostPort;
	boolean shutdown = false;

	public String getRequestInfo() throws Exception
	{
		System.out.println("Welcome to the Beast File Sharing System");
		System.out.println("Please type your request in the following format:");
		System.out.println("upload/download filename");

		BufferedReader keyboard = new BufferedReader( new InputStreamReader( System.in ) );
		String request = keyboard.readLine();

		String fileName = request.split(" ")[0];
		String fileType = request.split(" ")[1];

		while (!(fileType.toLowerCase.compareTo("upload") == 0) && !(fileType.toLowerCase.compareTo("download") == 0))
		 {
			System.out.println("Type upload/download");
			System.out.print(">>> ");

			BufferedReader keyboard = new BufferedReader( new InputStreamReader( System.in ) );
			fileType =  keyboard.readLine().toLowerCase();
		}

		File f = new File(fileName);
		while (!f.exists()) 
		{
			fileName = getNewFileName();

			if (fileName.compareTo("__quit__") == 0)
			{
				exit();
				return fileName;
			}

			f = new File(fileName);

		}
	}

	public String getNewFileName() throws Exception
	{
		System.out.println("The file you indicated does not exists.");
		System.out.println("Would you like to change the file name?");

		String response = "";

		while (!(response.compareTo("yes") == 0) && !(response.compareTo("no") == 0))
		{
			System.out.println("Type yes/no");
			System.out.print(">>> ");

			BufferedReader keyboard = new BufferedReader( new InputStreamReader( System.in ) );
			response =  keyboard.readLine().toLowerCase();
		}

		if (response.compareTo("yes") == 0)
		{
			System.out.println("Please type the name of the file >>>")
			keyboard = new BufferedReader( new InputStreamReader( System.in ) );
			response =  keyboard.readLine();
			return response;
		}
		else 
		{
			return "__quit__";
		}
	}

	public void setHostInfo(DatagramPacket packet) throws Exception
	{
		hostAddress = packet.getAddress();
		hostPort = packet.getPort();
	}

	@SuppressWarnings("deprecation")
	public void setHostInfo1(DatagramPacket packet) throws Exception
	{
		String data = new String(packet.getData());
			
		String[] data1 = data.split(",");

		hostAddress = InetAddress.getByName(data1[0].substring(1));
		hostPort = Integer.parseInt(data1[1]);
	}

	public void sendLine(String data) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), getHostAddress(), getHostPort());
		socket.send(packet);
	}

	public void uploadFile(String fileName) throws Exception
	{
		System.out.println("uploading to " + hostAddress + " with port " + hostPort + "...");
		Charset charset = Charset.forName("US-ASCII");

		String fileInfo = "__filename__," +fileName;
		sendLine(fileInfo);

		try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), charset)) 
		{
		    String line = null;
		    while ((line = reader.readLine()) != null) 
		    {
		        System.out.println(line);
		    	sendLine(line);
		    }
		    sendLine("__end__");
			System.out.println("File upload was succesfull");
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}

	public void downloadFile(String fileName) throws Exception
	{
		Charset charset = Charset.forName("US-ASCII");
		String line = null;

		try (PrintWriter writer = new PrintWriter(fileName, "UTF-8"))
		{
			while ((line = getPacketAndData()).compareTo("__end__") !=0)
			{
				System.out.println(line);
				line += "";
				if (line.compareTo("__dne__") ==0)
				{
				    System.out.println("The file you indicated is not hosted in on our servers");
				    System.out.println("Please try again with a different name");
				    Files.deleteIfExists(Paths.get(fileName));
				    exit();
				    return;
				}
				
			    writer.println(line);
			}
			
			System.out.println("File download was succesfull");
		} 
		catch (IOException x) 
		{
		    System.err.format("IOException: %s%n", x);
		}
	}

	public boolean isShutdown()
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