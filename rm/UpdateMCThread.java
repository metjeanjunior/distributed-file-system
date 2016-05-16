import java.net.*;

public class UpdateMCThread implements Runnable
{
	MCUtils mUtils;
	RMUtils rUtils;
	
	public UpdateMCThread(RMUtils rUtils) throws Exception
	{
		mUtils = new MCUtils(rUtils, 2);
		this.rUtils = rUtils;
	}

	public void run()
	{
		String fileInfo = null;
		String filename;
		int fileVer;

		while (true) 
		{
			System.out.println("MC worker group update up");
			try 
			{
				fileInfo = mUtils.readFromSocket();
				System.out.println("Incoming update fileInfo: " + fileInfo);
				filename = fileInfo.split(",")[0];
				fileVer = Integer.parseInt(fileInfo.split(",")[1]);

//				if(rUtils.selfUpload())
//				{ 
//					while (rUtils.selfUpload())
//						continue;
//					continue;
//				}
//
//				if(rUtils.getFileVersion(filename) == fileVer)
//					mUtils.passRecieve();
//				else
//					mUtils.recieveFile(filename);
//				
//				while (mUtils.isUploading())
//					continue;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			System.out.println("\t Upload finished");
		}
	}
}