package com.v2soft.styxandroid.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxandroid.library.StyxClientManager;
import com.v2soft.styxandroid.library.StyxFile;
import com.v2soft.styxandroid.library.exceptions.StyxException;
import com.v2soft.styxandroid.library.messages.base.enums.FileMode;

public class FileUploadTest implements RunTest {

	@Override
	public String doTest(StyxClientManager manager)
			throws InterruptedException, StyxException, IOException, TimeoutException
	{
		StyxFile file = new StyxFile(manager, "/About_these_files.odt");
		OutputStream os = file.create(FileMode.PERMISSION_BITMASK);
		FileInputStream fis = new FileInputStream("/sdcard/About_these_files.odt");
		
		byte[] buffer = new byte[1024];
		while (true)
		{
			int count = fis.read(buffer);
			if (count < 0)
				break;
			os.write(buffer, 0, count);
		}
		os.flush();
		
		return "OK!";
	}

}
