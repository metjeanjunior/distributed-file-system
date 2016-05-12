import java.io.*;
import java.net.*;

public class WorkerThread implements Runnable
{
	WorkerUtils wUtils;
	Datagrampacket packet;
	boolean debug = true;

	public WorkerThread(WorkerUtils wUtils, Datagrampacket packet)	
	{
		this.wUtils = wUtils;
		this.packet = packet;
	}

	public void run() throws
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		String data = wUtils.getDataFromPacket(packet);

		if(debug)
		{
			System.out.println("Incoming connection from" packet.getAddress() + ":" + packet.getPort());
			System.out.println("\t with the following info: " + data);
		}

		
	}
}