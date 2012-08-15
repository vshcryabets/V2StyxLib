package com.v2soft.styxlib.junit;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.enums.FileMode;


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

    // TVersion & TAttach
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
        mConnection.close();
    }
    
    // TVersion, Tattach, Twalk, create, write, clunk, open, read, remove
    @Test
    public void testFileCreation() throws IOException, StyxException, InterruptedException, TimeoutException {
        assertTrue(mConnection.connect());
        final StyxFile newFile = new StyxFile(mConnection, UUID.randomUUID().toString());
        final OutputStream out = newFile.create(FileMode.ReadOthersPermission.getMode() |
                FileMode.WriteOthersPermission.getMode());
        assertNotNull(out);
        byte [] testArray = new byte[]{1,3,5,7,11,13,17,19,23,29};
        out.write(testArray);
        out.close();
        final InputStream in = newFile.openForRead();
        assertNotNull(in);
        final byte [] readArray = new byte[testArray.length];
        int read = in.read(readArray);
        assertEquals(testArray.length, read);
        in.close();
        assertArrayEquals(testArray, readArray);
        newFile.delete();
        newFile.close();
        mConnection.close();
    }
    
    @Test
    public void testFSTree() throws IOException, StyxException, InterruptedException, TimeoutException {
        assertTrue(mConnection.connect());
        StyxFile newDirectory = new StyxFile(mConnection, UUID.randomUUID().toString());
        newDirectory.create(FileMode.ReadOthersPermission.getMode() |
                FileMode.WriteOthersPermission.getMode() |
                FileMode.Directory.getMode() );
        newDirectory.delete();
        newDirectory.close();
        mConnection.close();
    }

}
