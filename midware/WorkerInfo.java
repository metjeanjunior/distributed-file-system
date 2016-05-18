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

	public void flagRM() throws Exception
	{
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket packet = new DatagramPacket("__flagRM__".getBytes(), "__flagRM__".length(), rm.getAddress(), rm.getPort());
		socket.send(packet);
		socket.close();
	}
}