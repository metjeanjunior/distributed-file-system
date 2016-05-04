public class MDThread implements Runnable 
{
	private DatagramPacket packet;
	private MdServerUtils mdUtils;

	public MDThread(MdServerUtils mdUtils, DatagramPacket packet) 
	{
		this.mdUtils = mdUtils;
		this.packet = packet;	
	}

	public void run()
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		DatagramSocket socket = socket = new DatagramSocket();

		ByteArrayInputStream bin;
		DataInputStream dis;

		bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
		dis = new DataInputStream(bin);

		String data;

		try {
			data = dis.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
}