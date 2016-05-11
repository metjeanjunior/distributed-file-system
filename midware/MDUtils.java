import java.util.LinkedList;

public class MDUtils implements java.io.Serializable
{
	private LinkedList<RMInfo> rmList = new LinkedList<RMInfo>();
	// private LinkedList<WorkerInfo> workerList = new LinkedList<WorkerInfo>();

	public MdServerUtils(DatagramSocket socket)
	{
		this.socket = socket;
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
}