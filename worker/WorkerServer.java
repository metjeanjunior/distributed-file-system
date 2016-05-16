import java.io.*;
import java.net.*;
public class WorkerServer 
{
	public static void main(String[] args) throws Exception
	{
		DatagramSocket socket = new DatagramSocket();
		File dir = new File(socket.getLocalPort() + "");
		dir.mkdir();

		WorkerUtils wUtils = new WorkerUtils(socket);

//		int port = Integer.parseInt(args[1]);		
//		InetAddress address = InetAddress.getByName(args[0]);

//		For testing on a local machine
		 int port = 4576;		
		 InetAddress address = InetAddress.getByName("localhost");

		byte[] sbuf = new byte[1024];
		byte[] rbuf = new byte[1024];

		sbuf = "__w__".getBytes();

		DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);

		socket.send(packet);		
		wUtils.setUp();

		while(wUtils.isUpdating())
			continue;
		if (wUtils.isShutDown())
			return;

		Thread uploadThread = new Thread(new UploadMCThread(wUtils));
		uploadThread.start();
		Thread updateThread = new Thread(new UpdateMCThread(wUtils));
		updateThread.start();

		while(true)
		{
			System.out.println("Worker waiting for new requests...");
			packet = new DatagramPacket(rbuf, rbuf.length);	
			socket.receive(packet);
			System.out.println("Just recieved..." + wUtils.getDataFromPacket(packet));

			Thread thread = new Thread(new WorkerThread(wUtils, packet));
			thread.start();
			System.out.println("THread finished");
		}
	}	
}