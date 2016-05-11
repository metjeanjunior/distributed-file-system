import java.io.*;
import java.net.*;

public class RMInfo 
{
	DatagramPacket packet;
	int numWorkers = 0;

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

	public synchronized void incrNumWorkers()
	{
		numWorkers++;
	}

	public synchronized int getNumWorker()
	{
		return numWorkers;
	}
}