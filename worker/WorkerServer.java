import java.io.*;
import java.net.*;

public class WorkerServer 
{
	public static void main(String[] args) 
	{
		DatagramSocket socket = new DatagramSocket();	
		DatagramPacket packet;

		WorkerUtils WorkerUtils = new WorkerUtils(socket);

		int port = Integer.parseInt(args[1]);		
		InetAddress address = InetAddress.getByName(args[0]);

		byte[] sbuf = new byte[1024];
		byte[] rbuf = new byte[1024];

		sbuf = "__w__".getBytes();

		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);

		socket.send(packet);		
		wUtils.setUp();

		while(rUtils.isUpdating())
			continue;
		if (rUtils.isShutDown())
			return;

		while(true)
		{
			packet = new DatagramPacket(rbuf, rbuf.length);	
			socket.receive(packet);
			System.out.println("Just recieved..." + rUtils.getDataFromPacket(packet));

			Thread thread = new Thread(new WorkerThread(rUtils, packet));
			thread.start();
		}
	}	
}