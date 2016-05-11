public class MDThread implements Runnable 
{
	private DatagramPacket packet;
	private MdServerUtils mdUtils;
	boolean debug = true;

	public MDThread(MdServerUtils mdUtils, DatagramPacket packet) 
	{
		this.mdUtils = mdUtils;
		this.packet = packet;	
	}

	public void run() throws Exception
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		DatagramSocket socket = socket = new DatagramSocket();

		ByteArrayInputStream bin;
		DataInputStream dis;

		bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
		dis = new DataInputStream(bin);

		String data = dis.readLine();

		if (data.compareTo("__rm__") == 0)
		{
			if (debug)
				System.out.println("A Remote Manager just connected");

			if (mdUtils.getNumRM() == 3)
			{
				packet = new DatagramPacket("__quit".getBytes(), "__quit__".length(), packet.getAddress(), packet.getPort());
				DatagramSocket socket = new DatagramSocket();
				socket.send(packet);
				System.out.println("Connection from previous RM rejected (we're full)!");
				return;
			}

			RMInfo rmInfo = new RMInfo(packet);
			mdUtils.pushRM(rmInfo);

			if (debug)
				System.out.println("Added host at" + packet.getAddress());
		}
		else if (data.compareTo("__w__") == 0)
		{
			if (debug)
				System.out.println("A Worker just connected");
			if (mdUtils.getNumRM() == 0 || mdUtils.getNumRM() == 3)
			{
				packet = new DatagramPacket("__quit".getBytes(), "__quit__".length(), packet.getAddress(), packet.getPort());
				DatagramSocket socket = new DatagramSocket();
				socket.send(packet);
				System.out.println("Connection from previous Worker rejected (we're either full or empty)!");
				return;
			}

			RMinfo rm = mdUtils.getnextRM();
			
		}
		else
		{
			if (debug)
				System.out.println("A Client just connected");
			
		}
	}
}