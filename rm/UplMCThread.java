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
		System.out.println("UplRMC thread started");
		String fileInfo = null;

		try 
		{
			while (true) 
			{
				fileInfo = mUtils.readFromRSocket();
				System.out.println("Incoming RM upload file: " + fileInfo);

				// mUtils.flagSeflUpload(); // Allows both, leave
				// rUtils.flagSeflUpload();

				if (rUtils.selfUploading())
				{
					System.out.println("Incoming self upload passed");
					mUtils.passRRecieve();
				}
				else
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
