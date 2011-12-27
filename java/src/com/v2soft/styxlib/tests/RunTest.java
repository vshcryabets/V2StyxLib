package com.v2soft.styxlib.tests;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.exceptions.StyxException;

public interface RunTest {

	String doTest(StyxClientManager manager)
		throws InterruptedException, StyxException, IOException, TimeoutException;
	
}
