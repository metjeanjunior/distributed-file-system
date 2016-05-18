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
				fileInfo = mUtils.readFromSocket();
				System.out.println("Incoming update fileInfo: " + fileInfo);
				if (fileInfo.compareTo("__done__") == 0)
					continue;
				filename = fileInfo.split(",")[0];
				fileVer = Integer.parseInt(fileInfo.split(",")[1]);

				// if(wUtils.selfUpload())
				// { 
				// 	System.out.println("self Upload passed");
				// 	while (wUtils.selfUpload())
				// 		continue;
				// 	continue;
				// }

				if(wUtils.getFileVersion(filename) == fileVer && fileVer != -1)
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