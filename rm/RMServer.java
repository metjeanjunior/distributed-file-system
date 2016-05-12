import java.io.*;
import java.net.*;

public class RMServer 
{
	public static void main(String[] args) 
	{
		DatagramSocket socket = new DatagramSocket();	
		DatagramPacket packet;

		RMUtils rUtils = new RMUtils(socket);

		int port = Integer.parseInt(args[1]);		
		InetAddress address = InetAddress.getByName(args[0]);

		byte[] sbuf = new byte[1024];
		byte[] rbuf = new byte[1024];

		sbuf = "__rm__".getBytes();

		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);

		socket.send(packet);
		rUtils.setUp();

		while(rUtils.isUpdating())
			continue;
		if(rUtils.isShutDown)
			return;

		while(true)
		{
			packet = new DatagramPacket(rbuf, rbuf.length);	
			socket.receive(packet);
			System.out.println("Just recieved..." + rUtils.getDataFromPacket(packet));

			Thread thread = new Thread(new RMThread(rUtils, packet));
			thread.start();
		}
	}
}