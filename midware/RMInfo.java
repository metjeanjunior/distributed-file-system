import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class RMInfo 
{
	DatagramPacket packet;
	int numWorkers = 0;
	int numRoles = 0;
	boolean isUp = true;
	protected LinkedList<WorkerInfo> workerList = new LinkedList<WorkerInfo>();

	public RMInfo(DatagramPacket packet)
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

	public String getInfo()
	{
		return getAddress() + ":" + getPort();
	}

	public synchronized void incrRoles()
	{
		numRoles++;
	}

	public synchronized void decrRoles()
	{
		numRoles--;
	}

	public synchronized int getNumRoles()
	{
		return numRoles;
	}

	public synchronized void incrNumWorkers()
	{
		numWorkers++;
	}

	public synchronized int getNumWorker()
	{
		return numWorkers;
	}

	public synchronized boolean isUp()
	{
		return isUp;
	}

	public synchronized void kill()
	{
		isUp = false;
	}

	public synchronized void resurect() throws Exception
	{
		for (WorkerInfo worker : workerList)
			worker.flagRM();
		isUp = true;
	}
}