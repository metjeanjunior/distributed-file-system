public class RMThread implements Runnable
{
	RMUtils rUtils;
	Datagrampacket packet;
	public RMThread(RMUtils rUtils, Datagrampacket packet)
	{
		this.rUtils = rUtils;
		this.packet = packet;
	}

	public void run() throws 
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		String data = rUtils.getDataFromPacket(packet);

		if (data.compareTo("server") == 0)
		{
			rUtils.pushWorker(packet);
		}
		else if (data.split(",")[0].compareTo("client"))
		{
			if(data.split(",")[1].compareTo("upload"))
				rUtils.sendToUploader(packet);
			else
				rUtils.sendToDownloader(packet);
		}
		
	}
}