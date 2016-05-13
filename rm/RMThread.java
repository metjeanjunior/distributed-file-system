public class RMThread implements Runnable
{
	RMUtils rUtils;
	Datagrampacket packet;
	public RMThread(RMUtils rUtils, Datagrampacket packet)
	{
		this.rUtils = rUtils;
		this.packet = packet;
	}

	public void run() throws Exception
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		String data = rUtils.getDataFromPacket(packet);

		if (data.compareTo("__server__") == 0)
		{
			rUtils.pushWorker(packet);
		}
		else // if (data.split(",")[0].compareTo("client"))
		{
			if(data.split(",")[0].compareTo("upload"))
				rUtils.sendRolePacket(data, "upl+");
			else
				rUtils.sendToDownloader(data, "dwl");
		}

	}
}