import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;


public class MDUtils implements java.io.Serializable
{
	private LinkedList<RMInfo> rmList = new LinkedList<RMInfo>();
	Map<String, Integer> roleMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	// private LinkedList<WorkerInfo> workerList = new LinkedList<WorkerInfo>();

	public MdServerUtils(DatagramSocket socket)
	{
		this.socket = socket;
		initRoles();
	}

	public synchronized void pushRM(RMInfo rmInfo)
	{
		rmList.push(rmInfo);
	}

	public synchronized int getNumRM()
	{
		return rmList.size();
	}

	public RMInfo getnextRM()
	{

		RMInfo lowRM = rmList.get(0);
		for (RMInfo rm: rmList) 
			if (rm.getNumWorker() < lowRM.getNumWorker())
				lowRM = rm;

		// int lowRM = 0;
		// for (int x=0; x<rmList.size(); x++) 
		// 	if (rmList.get(x).getNumWorker() < rmList.get(lowRM).getNumWorker())
		// 		lowRM = rm;
			
		return lowRM;
	}

	public void initRoles()
	{
		roleMap.put("upl", 0);
		roleMap.put("upd", 0);
		roleMap.put("dwl", 0);
	}

	public void sendPacket(String data, int rmNum)
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
			rmList.get(rmNum).getAddress(), rmList.get(rmNum).getPort());
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		socket.close();
	}

	public void sendRolePacket(String data, String role)
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
			rmList.get(getRoleRep(role)).getAddress(), rmList.get(getRoleRep(role)).getPort());
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		socket.close();
	}

	public void sendGenericPacket(String data, InetAddress address, int port)
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), address, port)
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
		socket.close();
	}

	public synchronized void setRole(String role, int rmNum)
	{
		roleMap.put(role, rmNum);
	}

	public synchronized int getRoleRep(String role)
	{
		return roleMap.get(role);
	}

	public synchronized String nextRole()
	{
		String role;

		if (roleMap.get("upl") == roleMap.get("dwl"))
			return "dwl";
		if (roleMap.get("upl") == roleMap.get("upd"))
			return "upl";
		if (roleMap.get("dwl") == roleMap.get("upd"))
			return "dwl";

		return role;
	}
}