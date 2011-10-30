package com.v2soft.styxlib.tests;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.exceptions.StyxException;

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
