public class WorkerServer 
{
	public static void main(String[] args) 
	{
		DatagramSocket socket = new DatagramSocket();	
		DatagramPacket packet;

		WorkerUtils wUtils = new WorkerUtils(socket);

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
	}	
}