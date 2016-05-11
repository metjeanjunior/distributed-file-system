import java.io.*;
import java.net.*;

public class RMInfo 
{
	DatagramPacket packet;
	int numWorkers = 0;
	int numRoles = 0;
	boolean isUp = true;

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

	public synchronized void incrRoles
	{
		numRoles++;
	}

	public synchronized void decrRoles
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

	public synchronized resurect()
	{
		isUp = true;
	}
}