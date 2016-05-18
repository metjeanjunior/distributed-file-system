import java.net.*;

public class RMServer 
{
	public static void main(String[] args) throws Exception 
	{
		DatagramSocket socket = new DatagramSocket();	
		DatagramPacket packet;

		RMUtils rUtils = new RMUtils(socket);

//		int port = Integer.parseInt(args[1]);		
//		InetAddress address = InetAddress.getByName(args[0]);

//		For testing on a local machine
		 int port = 4576;		
		 InetAddress address = InetAddress.getByName("localhost");

		byte[] sbuf = new byte[1024];
		byte[] rbuf = new byte[1024];

		sbuf = "__rm__".getBytes();

		packet = new DatagramPacket(sbuf, sbuf.length, address, port);

		socket.send(packet);
		rUtils.setUp();

		while(rUtils.isUpdating())
			continue;
		if(rUtils.isShutDown())
			return;

		Thread uploadRThread = new Thread(new UplMCThread(rUtils));
		uploadRThread.start();
		// Thread uploadWThread = new Thread(new UplWMCThread(rUtils));
		// uploadWThread.start();

		while(true)
		{
			System.out.println("RM waiting new requests at " + socket.getLocalSocketAddress() + "...");
			packet = new DatagramPacket(rbuf, rbuf.length);	
			socket.receive(packet);
			System.out.println("\tJust recieved..." + rUtils.getDataFromPacket(packet));

			Thread thread = new Thread(new RMThread(rUtils, packet));
			thread.start();
		}
	}
}