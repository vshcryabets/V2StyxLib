package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.library.server.tcp.TCPServerManager;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.ULong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Styx buffers tests.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class PacketIOTests {

    private static final int PORT = 10234;
    private static final String FILE_NAME = "md5file";
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
        MemoryStyxFile md5 = new MemoryStyxFile(FILE_NAME){
            protected HashMap<ClientState, MessageDigest> mClientsMap = new HashMap<ClientState, MessageDigest>();
            @Override
            public boolean open(ClientState client, int mode)
                    throws IOException {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    mClientsMap.put(client, md);
                } catch (NoSuchAlgorithmException e) {
                    return false;
                }
                return super.open(client, mode);
            }
            @Override
            public void close(ClientState client) {
                mClientsMap.remove(client);
                super.close(client);
            }
            @Override
            public int write(ClientState client, byte[] data, ULong offset)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(client) ) {
                    mClientsMap.get(client).reset();
                    mClientsMap.get(client).update(data);
                }
                return super.write(client, data, offset);
            }
            @Override
            public long read(ClientState client, byte[] outbuffer, ULong offset, long count)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(client) ) {
                    byte[] digest = mClientsMap.get(client).digest();
                    if (count < digest.length) {
                        return 0;
                    } else {
                        System.arraycopy(digest, 0, outbuffer, 0, digest.length);
                        return digest.length;
                    }
                }
                return super.read(client, outbuffer, offset, count);
            }
        };
        MemoryStyxDirectory root = new MemoryStyxDirectory("root");
        root.addFile(md5);
        mServer = new TCPServerManager(InetAddress.getByName("127.0.0.1"),
                PORT,
                false,
                root);
        mServerThread = mServer.start();
    }

    @Test
    public void testMD5() throws IOException, StyxException, InterruptedException, TimeoutException, NoSuchAlgorithmException {
        Random random = new Random();
        MessageDigest digest = MessageDigest.getInstance("MD5");


        StyxClientConnection mConnection = new StyxClientConnection();
        byte[] someData = new byte[1024];
        byte [] remoteHash = new byte[16];

        assertTrue(mConnection.connect(
                new TCPClientChannelDriver(
                        InetAddress.getByName("localhost"), PORT, false, mConnection.getIOBufSize()),
                null, null));

        final StyxFile newFile = new StyxFile(mConnection, FILE_NAME);
        OutputStream output = newFile.openForWriteUnbuffered();
        InputStream input = newFile.openForReadUnbuffered();

        int count = random.nextInt(20)+20;
        for ( int i = 0; i< count; i++ ) {

            random.nextBytes(someData);
            digest.reset();
            byte [] localHash = digest.digest(someData);

            output.write(someData);
            int read = input.read(remoteHash);

            assertEquals("Wrong remote hash size", 16, read);
            assertArrayEquals("Wrong remote hash", localHash, remoteHash);
        }

        output.close();
        input.close();
        mConnection.close();

    }

}
