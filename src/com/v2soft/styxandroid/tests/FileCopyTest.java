package com.v2soft.styxandroid.tests;

import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxandroid.library.StyxClientManager;
import com.v2soft.styxandroid.library.StyxFile;
import com.v2soft.styxandroid.library.exceptions.StyxException;

public class FileCopyTest implements RunTest {

	@Override
	public String doTest(StyxClientManager manager)
			throws InterruptedException, StyxException, IOException 
	{
		StyxFile file = new StyxFile(manager, "/android-sdk_r06-linux_86.tgz");
		FileOutputStream fos = new FileOutputStream("/sdcard/android-sdk_r06-linux_86.tgz");
		
		try
		{
	        InputStream is = file.openForRead();
			byte[] buffer = new byte[1024];
			while (true)
			{
				int count = is.read(buffer);
				fos.write(buffer, 0, count);
			}
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		return "OK!";
	}

}
