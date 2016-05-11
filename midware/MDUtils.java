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
		RMInfo lowRM = rmList[0];
		int lowRM = 0;

		for (RMInfo rm: rmList) 
			if (getNumWorker.getNumWorker() < lowRM.getNumWorker())
				lowRM = rm;

		return lowRM;
	}

	public void initRoles()
	{
		roleMap.put("upl", -1);
		roleMap.put("upd", -1);
		roleMap.put("dwl", -1);
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

		
		
		return role;
	}
}