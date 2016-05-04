import java.util.LinkedList;

public class MDUtils implements java.io.Serializable
{
	private LinkedList<HostInfo> hostList = new LinkedList<HostInfo>();
	private LinkedList<WorkerInfo> workerList = new LinkedList<WorkerInfo>();

	public MdServerUtils(DatagramSocket socket)
	{
		this.socket = socket;
	}
}