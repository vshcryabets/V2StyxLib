package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.l6.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.l6.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Styx buffers tests.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class PacketIOTests {

    private static final int PORT = 10234;
    private static final String FILE_NAME = "md5file";
    private StyxServerManager mServer;

    @BeforeEach
    public void setUp() throws Exception {
        startServer();
    }

    @AfterEach
    public void shutDown() throws InterruptedException, IOException {
        mServer.closeAndWait();
    }

    private void startServer() throws StyxException, UnknownHostException {
        MemoryStyxFile md5 = new MemoryStyxFile(FILE_NAME){
            protected HashMap<ClientDetails, MessageDigest> mClientsMap = new HashMap<ClientDetails, MessageDigest>();
            @Override
            public boolean open(ClientDetails client, int mode)
                    throws StyxException {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    mClientsMap.put(client, md);
                } catch (NoSuchAlgorithmException e) {
                    return false;
                }
                return super.open(client, mode);
            }
            @Override
            public void close(ClientDetails client) {
                mClientsMap.remove(client);
                super.close(client);
            }
            @Override
            public int write(ClientDetails client, byte[] data, long offset)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(client) ) {
                    mClientsMap.get(client).reset();
                    mClientsMap.get(client).update(data);
                }
                return super.write(client, data, offset);
            }
            @Override
            public long read(ClientDetails client, byte[] outbuffer, long offset, long count)
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
        var localHost = InetAddress.getByName("127.0.0.1");
        var serverDriver = new TCPServerChannelDriver(localHost, PORT, false);
        var root = new MemoryStyxDirectory("root", serverDriver.getSerializer());
        root.addFile(md5);
        mServer = new StyxServerManager(
                root,
                Arrays.asList(serverDriver));
        mServer.start();
    }

    @Test
    public void testMD5() throws IOException, StyxException, InterruptedException, TimeoutException, NoSuchAlgorithmException {
        Random random = new Random();
        MessageDigest digest = MessageDigest.getInstance("MD5");


        Connection mConnection = new Connection(new CredentialsImpl("user", ""),
                new TCPClientChannelDriver(
                        InetAddress.getByName("localhost"), PORT, false));
        byte[] someData = new byte[1024];
        byte [] remoteHash = new byte[16];

        assertTrue(mConnection.connect());

        final StyxFile newFile = mConnection.open(FILE_NAME);
        OutputStream output = newFile.openForWriteUnbuffered();
        InputStream input = newFile.openForReadUnbuffered();

        int count = random.nextInt(20)+20;
        for ( int i = 0; i< count; i++ ) {

            random.nextBytes(someData);
            digest.reset();
            byte [] localHash = digest.digest(someData);

            output.write(someData);
            int read = input.read(remoteHash);

            assertEquals(16, read, "Wrong remote hash size");
            assertArrayEquals(localHash, remoteHash, "Wrong remote hash");
        }

        output.close();
        input.close();
        mConnection.close();

    }

}
