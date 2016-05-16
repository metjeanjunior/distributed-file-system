import java.io.*;
import java.net.*;
import java.text.*;
import java.util.Date;

public class MCUtils 
{
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private boolean isUploading = false;
	RMUtils rUtils;

	public MCUtils(RMUtils rUtils, int type) throws Exception
	{
		this.rUtils = rUtils;
		String mcInfo = rUtils.getRMMCInfo();
		group = InetAddress.getByName(mcInfo.split(",")[0].substring(1));

		if (type == 1)
			port = Integer.parseInt(mcInfo.split(",")[1]);
		else
			port = Integer.parseInt(mcInfo.split(",")[2]);

		socket = new MulticastSocket(port);
		socket.joinGroup(group);
	}

	public synchronized boolean isUploading()
	{
		return isUploading;
	}

	public synchronized String readFromSocket() throws Exception
	{
		String socketString = null; 
		byte[] buf = new byte[1000];
		DatagramPacket recv = new DatagramPacket(buf, buf.length);
		// System.out.println("Reading from MC socket...");
		socket.receive(recv);
		socketString = new String(recv.getData(), 0, recv.getLength());
		// System.out.println("read: " + socketString);
		return socketString;
	}

	public synchronized void recieveFile(String fileInfo) throws Exception
	{
		
	}

	public void passRecieve() throws Exception
	{
		isUploading = true;
		String line;
		while ((line = readFromSocket()).compareTo("__end__") != 0)
			continue;
		isUploading = false;
	}
}