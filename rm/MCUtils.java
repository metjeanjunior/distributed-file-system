import java.io.IOException;
import java.net.*;

public class MCUtils 
{
	RMUtils rUtils;

	InetAddress group;

	int rPort;
	int wPort;

	MulticastSocket rSocket;
	MulticastSocket wSocket;

	private boolean isUploading = false;

	public MCUtils(RMUtils rUtils, int type) throws Exception 
	{
		this.rUtils = rUtils;

		String rMCInfo = rUtils.getWMCInfo();
		String wMCInfo = rUtils.getRMMCInfo();

		group = InetAddress.getByName(rMCInfo.split(",")[0].substring(1));

		if (type == 1)
		{
			rPort = Integer.parseInt(rMCInfo.split(",")[1]);
			wPort = Integer.parseInt(wMCInfo.split(",")[1]);
		}
		else
		{
			rPort = Integer.parseInt(rMCInfo.split(",")[2]);
			wPort = Integer.parseInt(wMCInfo.split(",")[2]);
		}

		rSocket = new MulticastSocket(rPort);
		wSocket = new MulticastSocket(wPort);

		rSocket.joinGroup(group);
		wSocket.joinGroup(group);
	}

	public synchronized boolean isUploading()
	{
		return isUploading;
	}

	public synchronized String readFromRSocket() throws Exception
	{
		String socketString = null; 
		byte[] buf = new byte[1000];
		DatagramPacket recv = new DatagramPacket(buf, buf.length);
		rSocket.receive(recv);
		socketString = new String(recv.getData(), 0, recv.getLength());
		System.out.println("read: " + socketString);
		return socketString;
	}

	public synchronized String readFromWSocket() throws Exception
	{
		String socketString = null; 
		byte[] buf = new byte[1000];
		DatagramPacket recv = new DatagramPacket(buf, buf.length);
		wSocket.receive(recv);
		socketString = new String(recv.getData(), 0, recv.getLength());
		System.out.println("read: " + socketString);
		return socketString;
	}

	public synchronized void recieveRFile(String fileInfo) throws Exception
	{
		isUploading = true;
		sendToWMCUpl(fileInfo);

		String line;
		while ((line = readFromWSocket()).compareTo("__end__") != 0)
			sendToWMCUpl(line);
		sendToWMCUpl("__end__");
		isUploading = false;
	}

	public synchronized void recieveWFile(String fileInfo) throws Exception
	{
		isUploading = true;
		sendToRMCUpl(fileInfo);

		String line;
		while ((line = readFromWSocket()).compareTo("__end__") != 0)
			sendToRMCUpl(line);
		sendToRMCUpl("__end__");
		isUploading = false;
	}

	public synchronized void sendToRMCUpl(String data) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), group, rPort);
		rSocket.send(packet);
	}

	public synchronized void sendToWMCUpl(String data) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), group, wPort);
		wSocket.send(packet);
	}
}