package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.library.DualLinkClientConnection;
import com.v2soft.styxlib.library.IClient;
import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.DualStreams;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IChannelDriver;
import com.v2soft.styxlib.library.server.IClientChannelDriver;
import com.v2soft.styxlib.library.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.library.server.tcp.TCPDualLinkServerManager;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertNotNull;
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
    private TCPDualLinkServerManager mServer;
    private Thread[] mServerThreads;

    @Before
    public void setUp() throws Exception {
        startServer();
    }

    @After
    public void shutDown() throws InterruptedException, IOException {
        mServer.close();
        for ( Thread thread : mServerThreads ) {
            thread.join();
        }
    }

    private void startServer() throws IOException {
        MemoryStyxFile md5 = new MD5StyxFile();
        MemoryStyxDirectory root = new MemoryStyxDirectory("root");
        root.addFile(md5);
        mServer = new TCPDualLinkServerManager(InetAddress.getByName("127.0.0.1"),
                PORT,
                false,
                root);
        mServerThreads = mServer.start();
    }

    // TVersion & TAttach
    @Test
    public void testTwoWayExport() throws IOException, StyxException, InterruptedException, TimeoutException, NoSuchAlgorithmException {
        MemoryStyxFile md5 = new MD5StyxFile();
        MemoryStyxDirectory root = new MemoryStyxDirectory("clientroot");
        root.addFile(md5);
        DualLinkClientConnection connection = new DualLinkClientConnection();
        connection.export(root);

        Random random = new Random();
        byte[] someData = new byte[1024];
        random.nextBytes(someData);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte [] localHash = digest.digest(someData);

        byte [] remoteHash = new byte[16];

        assertTrue(connection.connect(new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT, false, connection.getIOBufSize()), null, null));
        final StyxFile newFile = new StyxFile(connection, MD5StyxFile.FILE_NAME);
        DualStreams streams = newFile.openForReadAndWrite();
        streams.output.write(someData);
        streams.output.flush();
        int read = streams.input.read(remoteHash);
        streams.close();
        assertEquals("Wrong remote hash size", 16, read);
        assertArrayEquals("Wrong remote hash", localHash, remoteHash);

        // reverse test
        List<IChannelDriver> drivers = mServer.getDrivers();
//        List<ClientState> clients = drivers.get(0).getClients();
//        IClient client = mServer.getClient(clients.get(0));
//        final StyxFile clientFile = new StyxFile(client, MD5StyxFile.FILE_NAME);
//        streams = newFile.openForReadAndWrite();
//        streams.output.write(someData);
//        streams.output.flush();
//        read = streams.input.read(remoteHash);
//        streams.close();
//        assertEquals("Wrong remote hash size", 16, read);
//        assertArrayEquals("Wrong remote hash", localHash, remoteHash);


        connection.close();

    }

    @Test
    public void testGetClientsFromClient() throws IOException, InterruptedException, TimeoutException, StyxException {
        DualLinkClientConnection connection = new DualLinkClientConnection();
        IClientChannelDriver driver = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false, connection.getIOBufSize());
        assertTrue(connection.connect(driver));
        Set<ClientState> clients = driver.getClients();
        assertNotNull(clients);
        assertEquals(1, clients.size());
        ClientState pseudoClient = clients.iterator().next();
        assertNotNull(pseudoClient);
        assertNotNull(pseudoClient.getDriver());
        assertEquals(driver, pseudoClient.getDriver());
        assertEquals(TCPClientChannelDriver.PSEUDO_CLIENT_ID, pseudoClient.getId());
        connection.close();
    }

    @Test
    public void testGetClientsFromServer() throws IOException, InterruptedException, TimeoutException, StyxException {
        DualLinkClientConnection connection = new DualLinkClientConnection();
        IClientChannelDriver driver = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false, connection.getIOBufSize());
        assertTrue(connection.connect(driver));

        DualLinkClientConnection connection2 = new DualLinkClientConnection();
        IClientChannelDriver driver2 = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false, connection2.getIOBufSize());
        assertTrue(connection2.connect(driver2));

        List<IChannelDriver> drivers = mServer.getDrivers();
        assertNotNull(drivers);
        assertEquals(1, drivers.size());
        Set<ClientState> clients = drivers.get(0).getClients();
        assertNotNull(clients);
        assertEquals(2, clients.size());

        String tag1 = driver.toString();
        String tag2 = driver2.toString();
        connection2.close();
        Thread.sleep(500);
        clients = drivers.get(0).getClients();
        assertNotNull(clients);
        assertEquals(1, clients.size());

        connection.close();
        Thread.sleep(500);
        clients = drivers.get(0).getClients();
        assertNotNull(clients);
        assertEquals(0, clients.size());
    }

}
