import java.net.*;

public class MDServer
{
	public static void main(String[] args) throws Exception 
	{
		// DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[0]));

		// For testing on local machine
		DatagramSocket socket = new DatagramSocket(4576);

		byte[] rbuf = new byte[1024];

		MDUtils mdUtils = new MDUtils(socket);
		System.out.println("Server is listening for new connection at port # " + socket.getLocalPort() 
			+ " from IP " + socket.getLocalSocketAddress());

		while (true)
		{			
			DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);		

			socket.receive(packet);

			// System.out.println("packet received");
			MDThread thread = new MDThread(mdUtils, packet);
			Thread toStartThread = new Thread(thread);
			toStartThread.start();
		}
	}
}