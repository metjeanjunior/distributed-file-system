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
				
			}
		}
		else if (data.compareTo("__w__") == 0)
		{
			if (debug)
				System.out.println("A Worker just connected");

		}
		else
		{
			if (debug)
				System.out.println("A Client just connected");

		}
	}
}