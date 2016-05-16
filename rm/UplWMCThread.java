import java.net.*;

public class UplWMCThread implements Runnable
{
	RMUtils rUtils;
	MCUtils mUtils;

	public UplWMCThread(RMUtils rUtils) throws Exception 
	{
		this.rUtils = rUtils;
		mUtils = new MCUtils(rUtils, 1);
	}

	public void run()
	{
		System.out.println("UplWMC thread started");
		String fileInfo = null;

		try 
		{
			while (true) 
			{
				fileInfo = mUtils.readFromWSocket();
				System.out.println("Incoming worker upload file: " + fileInfo);

				// mUtils.flagSeflUpload();
				// rUtils.flagSeflUpload();

				// if (rUtils.selfUploading())
				// 	mUtils.passWRecieve();
				// else
					mUtils.recieveWFile(fileInfo);

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
