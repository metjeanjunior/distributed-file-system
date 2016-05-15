import java.net.*;

public class UploadMCThread implements Runnable
{
	MCUtils mUtils;
	public UploadMCThread(WorkerUtils wUtils) throws Exception
	{
		mUtils = new MCUtils(wUtils);
	}

	public void run()
	{
		String filename = null;

		while (true) 
		{
			System.out.println("MC worker group up");
			try 
			{
				filename = mUtils.readFromSocket();
				System.out.println("Incoming file: " + filename);

				mUtils.recieveFile(filename);
				while (mUtils.isUploading())
					continue;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			System.out.println("\t Upload finished");
		}
	}
}