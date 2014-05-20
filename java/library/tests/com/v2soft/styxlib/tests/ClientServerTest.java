package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.DualStreams;
import com.v2soft.styxlib.library.io.StyxFileBufferedInputStream;
import com.v2soft.styxlib.library.messages.base.enums.FileMode;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.ULong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.zip.CRC32;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Client JUnit tests
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientServerTest {
    private static final int PORT = 10234;
    private StyxServerManager mServer;
    private Thread mServerThread;

    @Before
    public void setUp() throws Exception {
        startServer();
    }

    @After
    public void shutDown() throws InterruptedException, IOException {
        mServer.close();
        mServerThread.join();
    }

    private void startServer() throws IOException {
        MemoryStyxFile md5 = new MD5StyxFile();
        MemoryStyxDirectory root = new MemoryStyxDirectory("root");
        root.addFile(md5);
        mServer = new StyxServerManager(InetAddress.getByName("127.0.0.1"),
                PORT,
                false,
                root);
        mServerThread = mServer.start();
    }

    // TVersion & TAttach
    @Test
    public void testMD5() throws IOException, StyxException, InterruptedException, TimeoutException, NoSuchAlgorithmException {
        Random random = new Random();
        byte[] someData = new byte[1024];
        random.nextBytes(someData);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte [] localHash = digest.digest(someData);

        StyxClientConnection mConnection = new StyxClientConnection(InetAddress.getByName("127.0.0.1"), PORT, false);
        byte [] remoteHash = new byte[16];

        assertTrue(mConnection.connect()); // mConnection.connect();
        final StyxFile newFile = new StyxFile(mConnection, MD5StyxFile.FILE_NAME);
        DualStreams streams = newFile.openForReadAndWrite();
        streams.output.write(someData);
        streams.output.flush();
        int read = streams.input.read(remoteHash);
        streams.close();
        mConnection.close();

        assertEquals("Wrong remote hash size", 16, read);
        assertArrayEquals("Wrong remote hash", localHash, remoteHash);
    }
}
