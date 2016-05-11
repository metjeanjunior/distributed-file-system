public class RMUtils implements java.io.Serializable
{
	private DatagramSocket socket;
	protected LinkedList<WorkerInfo> workerList = new LinkedList<WorkerInfo>(); //FIXME: Change class name
	private boolean isUpdating = true;
	private boolean shutdown = false;

	MulticastSocket updateDirSocket; 
	
	

	public RMUtils(DatagramSocket socket)
	{
		this.socket = socket;
	}

	public boolean isUpdating()
	{
		return isUpdating;
	}

	public boolean isShutDown()
	{
		return shutdown;
	}

	public void setUp()
	{
		isUpdating = true;

		String updateDirInfo = getPacketAndData();

		if (updateDirInfo.compareTo("__quit__") == 0)
		{
			shutdown = true;
			isUpdating = false;
			System.out.println("Farm is already full. Try again later.");
			return;
		}

		System.out.println("Connected to MD");

		InetAddress updateDirGroup = InetAddress.getByName(updateDirInfo.split(",")[0]);
		int updateDirPort = updateDirInfo.split(",")[1];
		int pos = Integer.parseInt(updateDirInfo.split(",")[2]);

		updateDirSocket = new MulticastSocket(updateDirPort);
		updateDirSocket.joinGroup(updateDirGroup);
		isUpdating = false;
	}

	public synchronized  void sendPacket(String data, int workerNum) throws Exception
	{
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), 
			getWorkerAddress(workerNum), getWorkerPort(workerNum));
		socket.send(packet);
	}

	public synchronized void sendGenericPacket(String data, DatagramPacket genPacket) throws Exception
	{
		if (data.length() == 0)
		{
			socket.send(packet);
			return;
		}
		
		genPacket = new DatagramPacket(data.getBytes(), data.length(), genPacket.getAddress(), genPacket.getPort());
		socket.send(genPacket);
	}

	@SuppressWarnings("deprecation")
	public String getDataFromPacket(DatagramPacket packet) throws Exception
	{
		bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
		dis = new DataInputStream(bin);

		return dis.readLine();
	}

	public synchronized String getPacketAndData() throws Exception
	{
		byte[] rbuf = new byte[1024];
		packet = new DatagramPacket(rbuf, rbuf.length);		
		socket.receive(packet);

		return getDataFromPacket(packet);
	}

	public synchronized DatagramPacket getPacket() throws Exception
	{
		byte[] rbuf = new byte[1024];
		packet = new DatagramPacket(rbuf, rbuf.length);		
		socket.receive(packet);
		return packet;
	}

	public synchronized void pushWorker(DatagramPacket packet)
	{
		WorkerInfo workerInfo = new WorkerInfo(packet);
		workerList.push(workerInfo);
		// primary = primary == null ? workerInfo : primary;
		// primaryNum = primaryNum == -1 ? workerList.size()-1 :primaryNum;
	}


	// Returns the server that facilitates the download request from the client
	public String getDownloader()
	{

	}

	public void sendToDownloader()
	{
		
	}

	// Returns the server that facilitates the upload request from the client
	public String getUploadder()
	{

	}

	public void sendToUploader()
	{

	}

	// Returns the server that facilitates the update of the directory
	public String getUpdater()
	{

	}
}