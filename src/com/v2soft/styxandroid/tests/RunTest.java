package com.v2soft.styxandroid.tests;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxandroid.library.StyxClientManager;
import com.v2soft.styxandroid.library.exceptions.StyxException;

public interface RunTest {

	String doTest(StyxClientManager manager)
		throws InterruptedException, StyxException, IOException, TimeoutException;
	
}
