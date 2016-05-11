public class WorkerThread implements Runnable
{
	WorkerUtils wUtils;
	Datagrampacket packet;

	public WorkerThread(WorkerUtils wUtils, Datagrampacket packet)	
	{
		this.wUtils = wUtils;
		this.packet = packet;
	}

	public void run() throws
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		String data = wUtils.getDataFromPacket(packet);
	}
}