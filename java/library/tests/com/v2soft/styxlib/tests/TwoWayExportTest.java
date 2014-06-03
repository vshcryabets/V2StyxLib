package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.library.IClient;
import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.DualStreams;
import com.v2soft.styxlib.library.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.library.server.tcp.TCPServerManager;
import com.v2soft.styxlib.library.server.tcp.TCPTwoWayServerManager;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Client JUnit tests
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class TwoWayExportTest {
    private static final int PORT = 10234;
    private TCPTwoWayServerManager mServer;
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
        mServer = new TCPTwoWayServerManager(InetAddress.getByName("127.0.0.1"),
                PORT,
                false,
                root);
        mServerThread = mServer.start();
    }

    // TVersion & TAttach
    @Test
    public void testTwoWayExport() throws IOException, StyxException, InterruptedException, TimeoutException, NoSuchAlgorithmException {
        MemoryStyxFile md5 = new MD5StyxFile();
        MemoryStyxDirectory root = new MemoryStyxDirectory("clientroot");
        root.addFile(md5);
        StyxClientConnection mConnection = new StyxClientConnection();
        mConnection.export(root);


        Random random = new Random();
        byte[] someData = new byte[1024];
        random.nextBytes(someData);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte [] localHash = digest.digest(someData);

        byte [] remoteHash = new byte[16];

        assertTrue(mConnection.connect(new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT, false, mConnection.getIOBufSize()), null, null));
        final StyxFile newFile = new StyxFile(mConnection, MD5StyxFile.FILE_NAME);
        DualStreams streams = newFile.openForReadAndWrite();
        streams.output.write(someData);
        streams.output.flush();
        int read = streams.input.read(remoteHash);
        streams.close();
        assertEquals("Wrong remote hash size", 16, read);
        assertArrayEquals("Wrong remote hash", localHash, remoteHash);

        // reverse test
//        IClient client = mServer.getClient(ClientS);
//        final StyxFile clientFile = new StyxFile(client, MD5StyxFile.FILE_NAME);
//        streams = newFile.openForReadAndWrite();
//        streams.output.write(someData);
//        streams.output.flush();
//        read = streams.input.read(remoteHash);
//        streams.close();
//        assertEquals("Wrong remote hash size", 16, read);
//        assertArrayEquals("Wrong remote hash", localHash, remoteHash);


        mConnection.close();

    }
}
