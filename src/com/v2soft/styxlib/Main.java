package com.v2soft.styxlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.tests.FileListTest;
import com.v2soft.styxlib.tests.RunTest;

public class Main {
	
	public void doRunTest()
	{
		StyxClientManager manager = null;
		try
		{
			manager = new StyxClientManager(InetAddress.getByName("localhost"),
					8080, false, null, null);
			manager.connect();
//			System.out.println((new FileListTest()).doTest(manager));
			StyxFile file = new StyxFile(manager, "/audio/MasterVolume");
//			InputStream is = file.openForRead();
//			InputStreamReader in = new InputStreamReader(is, "UTF-8");
//			BufferedReader bin = new BufferedReader(in);
//			
//			System.out.println(bin.readLine());
//			bin.close();
//			in.close();
//			is.close();
			OutputStream out = file.openForWrite();
			String q = "75";
			out.write(q.getBytes());
			out.flush();
			out.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally {
			try
			{
				if (manager != null)
					manager.close();
			} catch (IOException e)
			{ }
		}
	}
	
	public static void main(String[] args) {
		Main obj = new Main();
		obj.doRunTest();
	}
}