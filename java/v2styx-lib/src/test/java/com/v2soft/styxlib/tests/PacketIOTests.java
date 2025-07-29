package com.v2soft.styxlib.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.l6.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.l6.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import com.v2soft.styxlib.utils.StyxSessionDI;
import com.v2soft.styxlib.utils.impl.StyxSessionDIImpl;

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

/**
 * Styx buffers tests.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class PacketIOTests {

    private static final int PORT = 10234;
    private static final String FILE_NAME = "md5file";
    private StyxServerManager mServer;
    private StyxSessionDI di = new StyxSessionDIImpl(false);
    private StyxServerManager.Configuration serverConfiguration;
    private TCPChannelDriver.InitConfiguration initConfiguration = new TCPChannelDriver.InitConfiguration(
            StyxServerManager.DEFAULT_IOUNIT,
            false,
            InetAddress.getLoopbackAddress(),
            PORT,
            di);

    @BeforeEach
    public void setUp() throws Exception {
        startServer();
    }

    @AfterEach
    public void shutDown() throws InterruptedException, IOException {
        mServer.closeAndWait();
    }

    private void startServer() throws StyxException, UnknownHostException {
        MemoryStyxFile md5 = new MemoryStyxFile(FILE_NAME, di){
            protected HashMap<Integer, MessageDigest> mClientsMap = new HashMap<>();
            @Override
            public boolean open(int clientId, int mode)
                    throws StyxException {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    mClientsMap.put(clientId, md);
                } catch (NoSuchAlgorithmException e) {
                    return false;
                }
                return super.open(clientId, mode);
            }
            @Override
            public void close(int clientId) {
                mClientsMap.remove(clientId);
                super.close(clientId);
            }
            @Override
            public int write(int clientId, byte[] data, long offset)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(clientId) ) {
                    mClientsMap.get(clientId).reset();
                    mClientsMap.get(clientId).update(data);
                }
                return super.write(clientId, data, offset);
            }
            @Override
            public int read(int clientId, byte[] outbuffer, long offset, int count)
                    throws StyxException {
                if ( mClientsMap.containsKey(clientId) ) {
                    byte[] digest = mClientsMap.get(clientId).digest();
                    if (count < digest.length) {
                        return 0;
                    } else {
                        System.arraycopy(digest, 0, outbuffer, 0, digest.length);
                        return digest.length;
                    }
                }
                return super.read(clientId, outbuffer, offset, count);
            }
        };
        var serverDriver = new TCPServerChannelDriver(di);
        var root = new MemoryStyxDirectory("root", di);
        root.addFile(md5);
        serverConfiguration = new StyxServerManager.Configuration(
                root,
                Arrays.asList(serverDriver),
                di,
                StyxServerManager.DEFAULT_IOUNIT);
        mServer = new StyxServerManager(serverConfiguration);
        serverDriver.prepare(initConfiguration);
        mServer.start();
    }

    @Test
    public void testMD5() throws IOException, StyxException, InterruptedException, TimeoutException, NoSuchAlgorithmException {
        Random random = new Random();
        MessageDigest digest = MessageDigest.getInstance("MD5");
        var driver = new TCPClientChannelDriver(di);
        var clientConfiguration = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver,
                di);
        Connection mConnection = new Connection(clientConfiguration);
        byte[] someData = new byte[1024];
        byte [] remoteHash = new byte[16];
        driver.prepare(initConfiguration);
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
