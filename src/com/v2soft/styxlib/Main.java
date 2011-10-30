package com.v2soft.styxlib;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.StyxFileInputStream;

public class Main {
	
	public void doRunTest()
	{
		StyxClientManager manager = null;
		try
		{
			manager = new StyxClientManager(InetAddress.getByName("localhost"),
					8080, false, null, null);
			manager.connect();
			StyxFile file = new StyxFile(manager, "test.dat");
			FileOutputStream out = new FileOutputStream("out.dat");
			StyxFileInputStream is = file.openForRead();
			byte buffer[] = new byte[4096];
			int readed = 0;
			long total = 0;
			while ( (readed = is.read(buffer)) > 0 ) {
			    total+=readed;
				System.out.println("Readed "+total);
				out.write(buffer, 0, readed);
			}
			out.close();
			is.close();

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