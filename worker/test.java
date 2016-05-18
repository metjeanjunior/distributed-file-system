import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.LinkedList;
import java.net.*;

public class test
{
	public static void main(String[] args) throws Exception
	{
		File directory = new File("57238");
		File[] fList = directory.listFiles();
		for (File file : fList)
		{
			System.out.println(fList.length);
		}
	}
}