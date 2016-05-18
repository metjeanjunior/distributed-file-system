import java.io.*;
import java.net.*;

public class MDThread implements Runnable 
{
	private DatagramPacket packet;
	private MDUtils mdUtils;
	boolean debug= true;

	public MDThread(MDUtils mdUtils, DatagramPacket packet) 
	{
		this.mdUtils = mdUtils;
		this.packet = packet;	
	}

	@SuppressWarnings("deprecation")
	public void run()
	{
		System.out.println(Thread.currentThread().getName() + " started w/ id " + Thread.currentThread().getId());

		try 
		{
			DatagramSocket socket = new DatagramSocket();

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
					if (mdUtils.exists(packet.getAddress()))
					{
						mdUtils.addOldRM(packet.getAddress());
					}
					else
					{
						packet = new DatagramPacket("__quit__".getBytes(), "__quit__".length(), packet.getAddress(), packet.getPort());
						socket.send(packet);
						System.out.println("Connection from previous RM rejected (we're full)!");
						return;
					}
				}

				// if (mdUtils.exists(packet.getAddress()))
				// 	mdUtils.addOldRM(packet.getAddress());
				// else
					mdUtils.pushRM(packet);

				if (debug)
					System.out.println("Added host at" + packet.getAddress() + ":" + packet.getPort());
			}
			else if (data.compareTo("__w__") == 0)
			{
				if (debug)
					System.out.println("A Worker from " + packet.getAddress() + ":" + packet.getPort() + " just connected");
				if (mdUtils.getNumRM() == 0 || mdUtils.subFarmIsFull())
				{
					packet = new DatagramPacket("__quit__".getBytes(), "__quit__".length(), packet.getAddress(), packet.getPort());
					socket.send(packet);
					System.out.println("Connection from previous Worker rejected (we're either full or empty)!");
					return;
				}

				RMInfo rm = mdUtils.getnextRM();
				String rmInfo = rm.getAddress() + "," + rm.getPort();
				mdUtils.sendGenericPacket(rmInfo, packet.getAddress(), packet.getPort());
				rm.incrNumWorkers();
				mdUtils.pushWorker(packet, rm);
				System.out.println("Worker was connected to " + rmInfo);
			}
			else
			{
				if (debug)
					System.out.println("A Client just connected");
				if (mdUtils.getNumRM() == 0)
				{
					packet = new DatagramPacket("__quit__".getBytes(), "__quit__".length(), packet.getAddress(), packet.getPort());
					socket.send(packet);
					System.out.println("Connection from previous client rejected (we're empty)!");
					return;
				}

				String cInfo = data.split(" ")[0] + ',' + data.split(" ")[1] + ','
					+ packet.getAddress() + "," + packet.getPort();
				if (data.split(" ")[0].compareTo("upload") == 0)
					mdUtils.sendRolePacket(cInfo, "upl");
				else
					mdUtils.sendRolePacket(cInfo, "dwl");
			}
			socket.close();
		} 
		catch (Exception e1) 
		{
			e1.printStackTrace();
		}
	}
}