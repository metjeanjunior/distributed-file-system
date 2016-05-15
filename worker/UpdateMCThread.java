import java.net.*;

public class UpdateMCThread implements Runnable
{
	MCUtils mUtils;
	WorkerUtils wUtils;
	
	public UpdateMCThread(WorkerUtils wUtils) throws Exception
	{
		mUtils = new MCUtils(wUtils, 2);
		this.wUtils = wUtils;
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
				System.out.println("Incoming fileInfo: " + fileInfo);
				fileInfo = mUtils.readFromSocket();
				filename = fileInfo.split(",")[0];
				fileVer = Integer.parseInt(fileInfo.split(",")[1]);

				if(wUtils.selfUpload())
				{ 
					while (wUtils.selfUpload())
						continue;
					continue;
				}

				if(wUtils.getFileVersion(filename) == fileVer)
					mUtils.passRecieve();
				else
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