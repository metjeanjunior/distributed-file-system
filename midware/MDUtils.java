import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.awt.List;


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

	// Using just one address w/ different ports
	public InetAddress getMCAddress()
	{
		return InetAddress.getByName("228.5.6.7");
	}

	public String getPortForRM(int rmNum)
	{
		Map<Integer, String> rmPortMap = Collections.synchronizedMap(new HashMap<Integer, String>());
		rmPortMap.put(0,"65003, 65004");
		rmPortMap.put(1,"65005, 65006");
		rmPortMap.put(2,"65007, 65008");

		// Using 3 for the communication between the actually RM
		rmPortMap.put(3,"65001,65002");
		return rmPortMap.get(rmNum);
	}

	public String getMCInfo(int rmNum)
	{
		return getMCAddress() + "," + getPortForRM(rmNum);
	}

	public synchronized void pushRM(DatagramPacket packet)
	{
		RMInfo rmInfo = new RMInfo(packet);
		rmList.push(rmInfo);
		int rmNum = rmList.size() -1;
		sendPacket(getMCInfo(rmNum), rmNum);
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