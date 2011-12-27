package com.v2soft.styxlib.tests;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.exceptions.StyxException;

public class TVersionTest {
    private StyxClientManager mManager;
    
    public TVersionTest(String server, int port) throws UnknownHostException {
        mManager = new  StyxClientManager(InetAddress.getByName(server),
                port, false);
    }
    
    public void start(int count) throws IOException, StyxException, InterruptedException, TimeoutException {
        mManager.connect();
        long startTime = System.currentTimeMillis();
        for ( int i = 0; i < count; i++ ) {
            mManager.sendVersionMessage();
        }
        long diff = System.currentTimeMillis()-startTime;
        System.out.println(String.format("\tTransmited %d messages\n\t" +
        		"Received %d messages\n\t" +
        		"Error %d messages\n\t" +
        		"Average time for connection %d ms", 
        		mManager.getMessenger().getTransmitedCount(),
        		mManager.getMessenger().getReceivedCount(),
        		mManager.getMessenger().getErrorsCount(),
        		diff/count
        		));
    }
}
