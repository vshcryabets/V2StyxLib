package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.DualLinkClientConnection;
import com.v2soft.styxlib.library.IClient;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IChannelDriver;
import com.v2soft.styxlib.library.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.library.server.tcp.TCPDualLinkServerManager;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.Credentials;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertNotNull;
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
//        TestLogListener clientlistener = new TestLogListener("");
//        TestLogListener serverlistener = new TestLogListener("\t");

        List<IChannelDriver> drivers = mServer.getDrivers();
        assertNotNull(drivers);
        assertEquals(1, drivers.size());
//        drivers.get(0).setLogListener(serverlistener);

        MemoryStyxFile md5 = new MD5StyxFile();
        MemoryStyxDirectory root = new MemoryStyxDirectory("clientroot");
        root.addFile(md5);
        DualLinkClientConnection connection = new DualLinkClientConnection();
        connection.export(root);
        IChannelDriver driver = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false, connection.getIOBufSize());
//        driver.setLogListener(clientlistener);

        assertTrue(connection.connect(driver));
        ClientServerTest.checkMD5Hash(connection);
        ClientServerTest.checkMD5Hash(connection);
        ClientServerTest.checkMD5Hash(connection);

        // reverse test
        Set<ClientState> clients = drivers.get(0).getClients();
        ClientState client = clients.iterator().next();
        IClient reverseConnection = mServer.getReverseConnectionForClient(client,
                new Credentials(null, null));
        assertNotNull("Can't retrieve reverse connection to client",reverseConnection);
        reverseConnection.connect();
        ClientServerTest.checkMD5Hash(reverseConnection);
        ClientServerTest.checkMD5Hash(reverseConnection);
        ClientServerTest.checkMD5Hash(reverseConnection);

        reverseConnection.close();
        connection.close();

    }

    @Test
    public void testGetClientsFromClient() throws IOException, InterruptedException, TimeoutException, StyxException {
        DualLinkClientConnection connection = new DualLinkClientConnection();
        IChannelDriver driver = new TCPClientChannelDriver(
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

        List<IChannelDriver> drivers = mServer.getDrivers();
        assertNotNull(drivers);
        assertEquals(1, drivers.size());

        DualLinkClientConnection connection = new DualLinkClientConnection();
        IChannelDriver driver = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false, connection.getIOBufSize());
        assertTrue(connection.connect(driver));

        DualLinkClientConnection connection2 = new DualLinkClientConnection();
        IChannelDriver driver2 = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false, connection2.getIOBufSize());
        assertTrue(connection2.connect(driver2));

        Set<ClientState> clients = drivers.get(0).getClients();
        assertNotNull(clients);
        assertEquals(2, clients.size());

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

    private class TestLogListener implements ILogListener {
        private String mPrefix;

        TestLogListener(String prefix) {
            mPrefix = prefix;
        }

        @Override
        public void onMessageReceived(IChannelDriver driver, ClientState client, StyxMessage message) {
            System.out.println(String.format("%sR %s client %s message %s %d", mPrefix,
                    driver.toString(),
                    client.toString(),
                    message.getType().toString(),
                    message.getTag()));
        }

        @Override
        public void onMessageTransmited(IChannelDriver driver, ClientState client, StyxMessage message) {
            System.out.println(String.format("%sS %s client %s message %s %d", mPrefix,
                    driver.toString(),
                    client.toString(),
                    message.getType().toString(),
                    message.getTag()));
        }

        @Override
        public void onException(IChannelDriver driver, Throwable err) {

        }
    }

}
