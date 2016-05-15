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
				System.out.println("Incoming connection from" + packet.getAddress() + ":" + packet.getPort());
				System.out.println("\t with the following info: " + data);
			}

			if (data.split(",")[0].compareTo("upload") == 0)
				wUtils.recieveFile(packet);
			else
				wUtils.sendFile(packet);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

	}
}