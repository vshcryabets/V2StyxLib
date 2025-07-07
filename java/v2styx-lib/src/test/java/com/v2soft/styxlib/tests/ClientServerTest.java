package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.IClient;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.l6.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import com.v2soft.styxlib.utils.StyxSessionDI;
import com.v2soft.styxlib.utils.StyxSessionDIImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Client JUnit tests
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientServerTest {
    private static final int PORT = 10234;
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

    private void startServer() throws IOException {
        var md5 = new MD5StyxFile(di);
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

    // TVersion & TAttach
    @Test
    public void testMD5() throws IOException, StyxException, InterruptedException, TimeoutException,
            NoSuchAlgorithmException {
        var driver = new TCPClientChannelDriver(di);
        var connection = new Connection(new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver,
                di));
        driver.prepare(initConfiguration);
        assertTrue(connection.connect());
        checkMD5Hash(connection);
        connection.close();
    }

    protected static void checkMD5Hash(IClient connection) throws NoSuchAlgorithmException,
            IOException {
        Random random = new Random();
        byte[] someData = new byte[1024];
        random.nextBytes(someData);

        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte [] localHash = digest.digest(someData);
        byte [] remoteHash = new byte[16];

        final StyxFile newFile = connection.open(MD5StyxFile.FILE_NAME);
        OutputStream outStream = newFile.openForWrite();
        InputStream inStream = newFile.openForRead();

        outStream.write(someData);
        outStream.flush();
        int read = inStream.read(remoteHash);

        inStream.close();
        outStream.close();
        newFile.close();
        assertEquals(16, read, "Wrong remote hash size");
        assertArrayEquals(localHash, remoteHash, "Wrong remote hash");
    }

    @Test
    public void testStat() throws IOException, InterruptedException, TimeoutException {
        var driver = new TCPClientChannelDriver(di);
        var clientConfiguration = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver,
                di);
        Connection connection = new Connection(clientConfiguration);
        driver.prepare(initConfiguration);
        assertTrue(connection.connect());

        StyxFile rootDir = connection.getRoot();
        var files = rootDir.listStat();
        assertEquals(1, files.size());
        connection.close();
    }
}
