import java.net.*;

public class UploadMCThread implements Runnable
{
	MCUtils mUtils;
	WorkerUtils wUtils;
	
	public UploadMCThread(WorkerUtils wUtils) throws Exception
	{
		mUtils = new MCUtils(wUtils, 1);
		this.wUtils = wUtils;
	}

	public void run()
	{
		String filename = null;

		while (true) 
		{
			System.out.println("MC worker group upload up");
			try 
			{
				filename = mUtils.readFromSocket();
				System.out.println("Incoming file: " + filename);

				if(wUtils.selfUpload())
				{
					while (wUtils.selfUpload())
						continue;
					continue;
				}
				mUtils.recieveFile(filename);
				while (mUtils.isUploading())
					continue;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			System.out.println("\t Upload thread finished");
		}
	}
}