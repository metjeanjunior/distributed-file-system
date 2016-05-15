import java.io.*;
import java.net.*;

public class MCUtils 
{
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private boolean isUploading = false;
	WorkerUtils wUtils;

	public MCUtils(WorkerUtils wUtils, int type) throws Exception
	{
		this.wUtils = wUtils;
		group = wUtils.getGroup();

		if (type == 1)
			port = wUtils.getUploadPort();
		else
			port = wUtils.getUpdatePort();

		socket = new MulticastSocket(port);
		socket.joinGroup(group);
	}

	public boolean isUploading()
	{
		return isUploading;
	}

	public String readFromSocket() throws Exception
	{
		String socketString = null; 
		byte[] buf = new byte[1000];
		DatagramPacket recv = new DatagramPacket(buf, buf.length);
		socket.receive(recv);
		socketString = new String(recv.getData(), 0, recv.getLength());
		System.out.println("read: " + socketString);
		return socketString;
	}

	public void recieveFile(String fileName) throws Exception
	{
		isUploading = true;
		while(wUtils.fileLockTaken(fileName))
			continue;

		wUtils.grabFileLock(fileName);
			PrintWriter writer = new PrintWriter("files/" + fileName, "UTF-8");

			String line;
			System.out.println("\t" + "Recieving...");
			while ((line = readFromSocket()).compareTo("__end__") != 0)
			{
			    System.out.println("\t" + line);
			    writer.println(line);
			}
			wUtils.incrementVersion(fileName); 
		wUtils.returnFileLock(fileName);

		writer.close();
		System.out.println("Finished upload");
		isUploading = false;
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