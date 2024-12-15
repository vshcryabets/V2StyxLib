package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.IClient;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.l6.io.DualStreams;
import com.v2soft.styxlib.l6.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.l6.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
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

    @BeforeEach
    public void setUp() throws Exception {
        startServer();
    }

    @AfterEach
    public void shutDown() throws InterruptedException, IOException {
        mServer.closeAndWait();
    }

    private void startServer() throws IOException {
        var md5 = new MD5StyxFile();
        var localHost = InetAddress.getByName("127.0.0.1");
        var serverDriver = new TCPServerChannelDriver(localHost, PORT, false);
        var root = new MemoryStyxDirectory("root", serverDriver.getSerializer());
        root.addFile(md5);
        mServer = new StyxServerManager(
                root,
                List.of(serverDriver));
        mServer.start();
    }

    // TVersion & TAttach
    @Test
    public void testMD5() throws IOException, StyxException, InterruptedException, TimeoutException,
            NoSuchAlgorithmException {
        IClient connection = new Connection(
                new CredentialsImpl("user", ""),
                new TCPClientChannelDriver(
                        InetAddress.getByName("127.0.0.1"), PORT, false));
        assertTrue(connection.connect());
        checkMD5Hash(connection);
        connection.close();
    }

    protected static void checkMD5Hash(IClient connection) throws NoSuchAlgorithmException, InterruptedException,
            TimeoutException, IOException {
        Random random = new Random();
        byte[] someData = new byte[1024];
        random.nextBytes(someData);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte [] localHash = digest.digest(someData);
        byte [] remoteHash = new byte[16];
        final StyxFile newFile = new StyxFile(connection, MD5StyxFile.FILE_NAME);
        DualStreams streams = newFile.openForReadAndWrite();
        streams.output.write(someData);
        streams.output.flush();
        int read = streams.input.read(remoteHash);
        streams.close();
        newFile.close();
        assertEquals(16, read, "Wrong remote hash size");
        assertArrayEquals(localHash, remoteHash, "Wrong remote hash");
    }

    @Test
    public void testStat() throws IOException, InterruptedException, TimeoutException {
        Connection connection = new Connection(
                new CredentialsImpl("user", ""),
                new TCPClientChannelDriver(
                        InetAddress.getByName("127.0.0.1"), PORT, false));
        assertTrue(connection.connect());

        StyxFile rootDir = connection.getRoot();
        var files = rootDir.listStat();
        assertEquals(1, files.size());
        connection.close();
    }
}
