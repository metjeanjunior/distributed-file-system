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
		String fileName = utils.getFileName();

		ByteArrayInputStream bin;
		DataInputStream dis;
		
//		int port = Integer.parseInt(args[1]);		
//		InetAddress address = InetAddress.getByName(args[0]);

//		For testing on a local machine
		int port = 4576;		
		InetAddress address = InetAddress.getByName("localhost");

		byte rbuf[] = new byte[1024];

		DatagramPacket packet = new DatagramPacket(fileName.getBytes(), fileName.length(), address, port);

		// connects to middleware
		socket.send(packet);

		// Recieve host packet w/ tcpPortNum
		packet = new DatagramPacket(rbuf, rbuf.length);
		socket.receive(packet);
		utils.setHostInfo(packet);
		String fileType = utils.getFileType();

		if (fileType.compareTo("upload") == 0)
		{
			File f = new File(fileName);
			while (!f.exists()) 
			{
				fileName = utils.getNewFileName();
				f = new File(fileName);

				if (fileName.compareTo("__quit__") == 0)
				{
					utils.exit();
					return;
				}
			}

			String data = fileType + "," + fileName;
			packet = new DatagramPacket(data.getBytes(), data.length(), utils.getHostAddress(), utils.getHostPort());
			socket.send(packet);
			// utils.sendPacket(fileType);			
			System.out.println("Connecting to server...");
			utils.uploadFile();
		}

		else
		{
			String data = fileType + "," + fileName;
			packet = new DatagramPacket(data.getBytes(), data.length(), utils.getHostAddress(), utils.getHostPort());
			socket.send(packet);
			System.out.println("Waiting for file from Server...");
			utils.downloadFile();
		}

		utils.exit();
		socket.close();
	}
}
