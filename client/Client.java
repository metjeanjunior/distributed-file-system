import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class Client 
{
	public static void main(String[] args) throws Exception 
	{
		DatagramSocket socket = new DatagramSocket();		
		ClientUtils utils = new ClientUtils(socket);
		String request = utils.getRequestInfo();

		if (request.compareTo("__quit__") == 0) 
			return;

		String fileName = request.split(" ")[1];

		ByteArrayInputStream bin;
		DataInputStream dis;
		
//		int port = Integer.parseInt(args[1]);		
//		InetAddress address = InetAddress.getByName(args[0]);

//		For testing on a local machine
		int port = 4576;		
		InetAddress address = InetAddress.getByName("localhost");

		byte rbuf[] = new byte[1024];

		DatagramPacket packet = new DatagramPacket(request.getBytes(), request.length(), address, port);

		// connects to middleware
		socket.send(packet);

		// Recieve host packet
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		utils.setHostInfo(packet);

		if (request.split(" ")[0].compareTo("upload") == 0)
		{
			System.out.println("Connecting to server...");
			utils.uploadFile(fileName);
		}

		else
		{
			System.out.println("Waiting for file from Server...");
			utils.downloadFile(fileName);
		}

		utils.exit();
		socket.close();
	}
}
