import java.io.*;
import java.net.*;

public class WorkerThread implements Runnable
{
	WorkerUtils wUtils;
	DatagramPacket packet;
	boolean debug = true;

	public WorkerThread(WorkerUtils wUtils, DatagramPacket packet)	
	{
		this.wUtils = wUtils;
		this.packet = packet;
	}

	public void run()
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		try 
		{
			 String data = wUtils.getDataFromPacket(packet);

			if(debug)
			{
				System.out.println("\tIncoming connection from" + packet.getAddress() + ":" + packet.getPort());
				System.out.println("\t\t with the following info: " + data);
			}

			if (data.compareTo("__update__") == 0)
				wUtils.update(packet);
			else if (data.compareTo("__rqfv__") == 0)
				wUtils.sendFileVer(packet);
			else if (data.split(",")[0].compareTo("upload") == 0)
				wUtils.recieveFile(packet);
			else
				wUtils.sendFile(packet);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		System.out.println("\tRequest was processed succesfully");
	}
}