package com.v2soft.styxlib.junit;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.exceptions.StyxException;


public class ConnectionTest {
    private StyxClientConnection mConnection;

    @Before
    public void setUp() throws Exception {
        mConnection = new StyxClientConnection(InetAddress.getByName("localhost"), 8080, false);
    }

    @After
    public void tearDown() throws Exception {
        mConnection.close();
    }

    @Test
    public void testConnection() throws IOException, StyxException, InterruptedException, TimeoutException {
        int count = 100;
        assertTrue(mConnection.connect());
        long startTime = System.currentTimeMillis();
        for ( int i = 0; i < count; i++ ) {
            mConnection.sendVersionMessage();
        }
        long diff = System.currentTimeMillis()-startTime;
        System.out.println(String.format("\tTransmited %d messages\n\t" +
                "Received %d messages\n\t" +
                "Error %d messages\n\t" +
                "Average time for connection %d ms", 
                mConnection.getMessenger().getTransmitedCount(),
                mConnection.getMessenger().getReceivedCount(),
                mConnection.getMessenger().getErrorsCount(),
                diff/count
                ));
    }
    
    @Test
    public void testFileCreation() throws IOException, StyxException, InterruptedException, TimeoutException {
        assertTrue(mConnection.connect());
        StyxFile newFile = new StyxFile(mConnection, "testFile3925");
        OutputStream out = newFile.create(0x6FF);
        assertNotNull(out);
        byte [] testArray = new byte[]{1,3,5,7,11,13,17,19,23,29};
        out.write(testArray);
        out.close();
        InputStream in = newFile.openForRead();
        assertNotNull(in);
        byte [] readArray = new byte[testArray.length];
        int read = in.read(readArray);
        assertEquals(testArray.length, read);
        in.close();
        assertArrayEquals(testArray, readArray);
        newFile.delete();
        
    }

}
