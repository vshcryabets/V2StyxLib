package com.v2soft.styxlib.tests;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.exceptions.StyxException;

public interface RunTest {

	String doTest(StyxClientConnection manager)
		throws InterruptedException, StyxException, IOException, TimeoutException;
	
}
