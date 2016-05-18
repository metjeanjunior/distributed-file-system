import java.io.*;
import java.net.*;

public class WorkerInfo
{
	DatagramPacket packet;
	RMInfo rm;

	public WorkerInfo(DatagramPacket packet, RMInfo rm)
	{
		this.packet = packet;
		this.rm = rm;
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
}