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
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.enums.FileMode;

/**
 * Client JUnit tests
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ConnectionTest {
    private StyxClientConnection mConnection;

    @Before
    public void setUp() throws Exception {
        mConnection = new StyxClientConnection(InetAddress.getByName("localhost"), 
                8080, null);
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
//        System.out.println(String.format("\tTransmited %d messages\n\t" +
//                "Received %d messages\n\t" +
//                "Error %d messages\n\t" +
//                "Average time for connection %d ms", 
//                mConnection.getMessenger().getTransmitedCount(),
//                mConnection.getMessenger().getReceivedCount(),
//                mConnection.getMessenger().getErrorsCount(),
//                diff/count
//                ));
        mConnection.close();
    }

    // TVersion, Tattach, Twalk, create, write, clunk, open, read, remove
    @Test
    public void testFileCreation() throws IOException, StyxException, InterruptedException, TimeoutException {
        assertTrue(mConnection.connect());
        final StyxFile newFile = new StyxFile(mConnection, UUID.randomUUID().toString());
        newFile.create(FileMode.ReadOthersPermission.getMode() |
                FileMode.WriteOthersPermission.getMode());
        final OutputStream out = newFile.openForWrite();
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
        // create 2 directory and 4 sub-directories
        String nameA = UUID.randomUUID().toString();
        String nameB = UUID.randomUUID().toString();
        String nameAA = nameA+StyxFile.SEPARATOR+UUID.randomUUID().toString();
        String nameAB = nameA+StyxFile.SEPARATOR+UUID.randomUUID().toString();
        String nameBA = UUID.randomUUID().toString();
        String nameBB = UUID.randomUUID().toString();
        String nameBC = nameB+StyxFile.SEPARATOR+UUID.randomUUID().toString();

        StyxFile dirBC = null;
        try {
            dirBC = new StyxFile(mConnection, nameBC);
            dirBC.create(FileMode.ReadOthersPermission.getMode() |
                    FileMode.WriteOthersPermission.getMode() |
                    FileMode.Directory.getMode() );
        } catch (StyxErrorMessageException e) {
            dirBC = null; // we should get an error
        }
        assertNull(dirBC); 


        final StyxFile dirA = new StyxFile(mConnection, nameA);
        final StyxFile dirB = new StyxFile(mConnection, nameB);
        final StyxFile dirAA = new StyxFile(mConnection, nameAA);
        final StyxFile dirAB = new StyxFile(mConnection, nameAB);
        

        dirA.create(FileMode.PERMISSION_BITMASK | FileMode.Directory.getMode());
        dirB.create(FileMode.PERMISSION_BITMASK | FileMode.Directory.getMode());
        dirAA.create(FileMode.PERMISSION_BITMASK | FileMode.Directory.getMode());
        dirAB.create(FileMode.PERMISSION_BITMASK | FileMode.Directory.getMode());
        // test other way to create file (with specified parent) 
        final StyxFile dirBA = new StyxFile(mConnection, nameBA, dirB);
        final StyxFile dirBB = new StyxFile(mConnection, nameBB, dirB);
        dirBA.create(FileMode.PERMISSION_BITMASK | FileMode.Directory.getMode());
        dirBB.create(FileMode.PERMISSION_BITMASK | FileMode.Directory.getMode());

        try {
            dirA.delete();
            assertTrue("Delete should return an error", false);
        } catch (StyxErrorMessageException e) {
        }
        // we lost FID, restore it
        dirA.getFID();

        dirBB.delete();
        dirBB.close();
        dirBA.delete();
        dirBA.close();

        dirAB.delete();
        dirAB.close();
        dirAA.delete();
        dirAA.close();
        
        dirA.delete();
        dirA.close();
        dirB.delete();
        dirB.close();
        mConnection.close();
    }

}
