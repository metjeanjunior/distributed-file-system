import java.net.*;

public class RMThread implements Runnable
{
	RMUtils rUtils;
	DatagramPacket packet;
	public RMThread(RMUtils rUtils, DatagramPacket packet)
	{
		this.rUtils = rUtils;
		this.packet = packet;
	}

	public void run() 
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		try 
		{
			String data = rUtils.getDataFromPacket(packet);
			
			if (data.compareTo("__server__") == 0)
			{
				rUtils.pushWorker(packet);
			}
			else // if (data.split(",")[0].compareTo("client"))
			{
				if(data.split(",")[0].compareTo("upload") == 0)
					rUtils.sendRolePacket(data, "upl+");
				else
					rUtils.sendRolePacket(data, "dwl");
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}


	}
}