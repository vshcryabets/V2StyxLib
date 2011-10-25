package com.v2soft.styxandroid.tests;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxandroid.library.StyxClientManager;
import com.v2soft.styxandroid.library.exceptions.StyxException;

public class FileListTest implements RunTest {

	@Override
	public String doTest(StyxClientManager manager)
		throws InterruptedException, StyxException, IOException, TimeoutException
	{
		String[] files = manager.getRoot().list();
		
		String result = "";
		for (String item : files)
		{
			if (!result.equals(""))
				result += "\n";
			result += item;
		}
		
		return result;
	}

}
