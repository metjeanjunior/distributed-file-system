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

			if (data.compareTo("__ping__") == 0)
			{
				DatagramSocket pingSocket = new DatagramSocket();
				DatagramPacket pingPacket = new DatagramPacket("__ping__".getBytes(), "__ping__".length(),
					packet.getAddress(), packet.getPort());
				pingSocket.send(pingPacket);
				System.out.println("Serviced ping request");
				pingSocket.close();
			}
			else if (data.compareTo("__update__") == 0)
			{
				System.out.println("update request came in");
				rUtils.serveUpd(packet);
			}
			else if (data.compareTo("__server__") == 0)
			{
				rUtils.pushWorker(packet);
			}
			else // if (data.split(",")[0].compareTo("client"))
			{
				System.out.println("\tClient request recieved");
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