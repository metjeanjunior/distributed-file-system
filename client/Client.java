import java.io.*;
import java.net.*;

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
		System.out.println("Connecting to MD...");
		socket.send(packet);

		// Recieve host packet
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		utils.setHostInfo(packet);

		if (utils.isShutDown())
			return;

		if (request.split(" ")[0].compareTo("upload") == 0)
			utils.uploadFile(fileName);
		else
			utils.downloadFile(fileName);

		utils.exit();
		socket.close();
	}
}