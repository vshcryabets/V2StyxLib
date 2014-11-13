package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.DualLinkClientConnection;
import com.v2soft.styxlib.library.IClient;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPDualLinkServerManager;
import com.v2soft.styxlib.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.vfs.MemoryStyxFile;
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
                false, connection.getConnectionDetails().getIOUnit());
//        driver.setLogListener(clientlistener);

        assertTrue(connection.connect(driver));
        ClientServerTest.checkMD5Hash(connection);
        ClientServerTest.checkMD5Hash(connection);
        ClientServerTest.checkMD5Hash(connection);

        // reverse test
        Set<ClientDetails> clientDetailses = drivers.get(0).getClients();
        ClientDetails clientDetails = clientDetailses.iterator().next();
        IClient reverseConnection = mServer.getReverseConnectionForClient(clientDetails,
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
                false, connection.getConnectionDetails().getIOUnit());
        assertTrue(connection.connect(driver));
        Set<ClientDetails> clientDetailses = driver.getClients();
        assertNotNull(clientDetailses);
        assertEquals(1, clientDetailses.size());
        ClientDetails pseudoClientDetails = clientDetailses.iterator().next();
        assertNotNull(pseudoClientDetails);
        assertNotNull(pseudoClientDetails.getDriver());
        assertEquals(driver, pseudoClientDetails.getDriver());
        assertEquals(TCPClientChannelDriver.PSEUDO_CLIENT_ID, pseudoClientDetails.getId());
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
                false, connection.getConnectionDetails().getIOUnit());
        assertTrue(connection.connect(driver));

        DualLinkClientConnection connection2 = new DualLinkClientConnection();
        IChannelDriver driver2 = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false, connection2.getConnectionDetails().getIOUnit());
        assertTrue(connection2.connect(driver2));

        Set<ClientDetails> clientDetailses = drivers.get(0).getClients();
        assertNotNull(clientDetailses);
        assertEquals(2, clientDetailses.size());

        connection2.close();
        Thread.sleep(500);
        clientDetailses = drivers.get(0).getClients();
        assertNotNull(clientDetailses);
        assertEquals(1, clientDetailses.size());

        connection.close();
        Thread.sleep(500);
        clientDetailses = drivers.get(0).getClients();
        assertNotNull(clientDetailses);
        assertEquals(0, clientDetailses.size());
    }

    private class TestLogListener implements ILogListener {
        private String mPrefix;

        TestLogListener(String prefix) {
            mPrefix = prefix;
        }

        @Override
        public void onMessageReceived(IChannelDriver driver, ClientDetails clientDetails, StyxMessage message) {
            System.out.println(String.format("%sR %s client %s message %s %d", mPrefix,
                    driver.toString(),
                    clientDetails.toString(),
                    message.getType().toString(),
                    message.getTag()));
        }

        @Override
        public void onMessageTransmited(IChannelDriver driver, ClientDetails clientDetails, StyxMessage message) {
            System.out.println(String.format("%sS %s client %s message %s %d", mPrefix,
                    driver.toString(),
                    clientDetails.toString(),
                    message.getType().toString(),
                    message.getTag()));
        }

        @Override
        public void onException(IChannelDriver driver, Throwable err) {

        }
    }

}
