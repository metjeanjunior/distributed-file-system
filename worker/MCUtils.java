import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class MCUtils 
{
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private boolean isUploading = false;
	WorkerUtils wUtils;

	public MCUtils(WorkerUtils wUtils) throws Exception
	{
		this.wUtils = wUtils;
		group = wUtils.getGroup();
		port = wUtils.getUploadPort();
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
			System.out.println("\t" + "Recieving...");

			String line;
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
}