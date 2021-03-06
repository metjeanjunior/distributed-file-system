import java.net.*;

public class WorkerInfo 
{
	DatagramPacket packet;
	boolean isUp = true;

	public WorkerInfo(DatagramPacket packet)
	{
		this.packet = packet;
	}

	public int getPort()
	{
		return  packet.getPort();
	}

	public InetAddress getAddress()
	{
		return packet.getAddress();
	}

	public synchronized boolean isUp()
	{
		return isUp;
	}

	public synchronized void kill()
	{
		isUp = false;
	}

	public synchronized void resurect()
	{
		isUp = true;
	}
}