import java.net.*;

public class UploadMCThread implements Runnable
{
	MCUtils mUtils;
	RMUtils rUtils;
	
	public UploadMCThread(RMUtils rUtils) throws Exception
	{
		mUtils = new MCUtils(rUtils, 1);
		this.rUtils = rUtils;
	}

	public void run()
	{
		String fileInfo = null;

		while (true) 
		{
			System.out.println("MC RM group upload up");
			try 
			{
				fileInfo = mUtils.readFromSocket();
				System.out.println("Incoming upload file: " + fileInfo);

				
				mUtils.recieveFile(fileInfo);
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