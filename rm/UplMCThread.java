import java.net.*;

public class UplMCThread implements Runnable
{
	RMUtils rUtils;
	MCUtils mUtils;

	public UplMCThread(RMUtils rUtils) throws Exception 
	{
		this.rUtils = rUtils;
		mUtils = new MCUtils(rUtils, 1);
	}

	public void run()
	{
		String fileInfo = null;

			try 
			{
				while (true) 
				{
					fileInfo = mUtils.readFromRSocket();
					System.out.println("Incoming RM upload file: " + fileInfo);

					mUtils.recieveRFile(fileInfo);
					while (mUtils.isUploading())
							continue;
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}

		System.out.println("\t Upload thread finished");
	}
}
