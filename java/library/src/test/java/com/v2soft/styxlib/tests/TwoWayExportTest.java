package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.StyxFile;
import com.v2soft.styxlib.library.DualLinkClientConnection;
import com.v2soft.styxlib.IClient;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.StyxUnbufferedOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.types.ULong;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPDualLinkServerManager;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.Credentials;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private Charset mCharset = Charset.forName("utf-8");

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
        List<IChannelDriver> drivers = mServer.getDrivers();
        assertNotNull(drivers);
        assertEquals(1, drivers.size());

        MemoryStyxFile md5 = new MD5StyxFile();
        MemoryStyxDirectory root = new MemoryStyxDirectory("clientroot");
        root.addFile(md5);
        DualLinkClientConnection connection = new DualLinkClientConnection();
        connection.export(root);
        IChannelDriver driver = new TCPClientChannelDriver(InetAddress.getByName("127.0.0.1"), PORT, false);

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
                false);
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
                false);
        assertTrue(connection.connect(driver));

        DualLinkClientConnection connection2 = new DualLinkClientConnection();
        IChannelDriver driver2 = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false);
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

    @Test
    public void testChat() throws IOException, InterruptedException, TimeoutException, StyxException {
        int count = 5;
        Object syncObject = new Object();

        String messages[] = new String[count];
        DualLinkClientConnection clients[] = new DualLinkClientConnection[count];
        IChannelDriver clientDrivers[] = new TCPClientChannelDriver[count];
//        IVirtualStyxFile clientFiles[] = new IVirtualStyxFile[count];
        StyxFile reverseFiles[] = new StyxFile[count];
        ChatStyxFile clientFiles[] = new ChatStyxFile[count];
        final OutputStream outputs[] = new OutputStream[count];

        // prepare server chat file
        MemoryStyxFile chatServer = new MemoryStyxFile("chat") {
            @Override
            public int write(ClientDetails clientDetails, byte[] data, ULong offset) throws StyxErrorMessageException {
                String message = new String(data, mCharset);
                // sent this message to other clients
                for (OutputStream out : outputs) {
                    try {
                        out.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return data.length;
            }
        };
        ((MemoryStyxDirectory)mServer.getRoot()).addFile(chatServer);

        // prepare messages
        for (int i = 0; i < count; i++) {
            messages[i] = UUID.randomUUID().toString();
        }
        // create clients
        for ( int i = 0; i < count; i++ ) {
            clients[i] = new DualLinkClientConnection();
            clientDrivers[i] = new TCPClientChannelDriver(
                    InetAddress.getByName("127.0.0.1"), PORT,
                    false);
            String marker = ( i == 0 ? messages[count-1] : messages[i-1]);
            clientFiles[i] = new ChatStyxFile("chat", messages[i], marker, (i == 0));
            MemoryStyxDirectory root = new MemoryStyxDirectory("clientroot");
            root.addFile(clientFiles[i]);
            clients[i].export(root);
            assertTrue(clients[i].connect(clientDrivers[i]));
            clientFiles[i].attachToserver(clients[i]);
        }

        // prepare server
        List<IChannelDriver> drivers = mServer.getDrivers();
        Set<ClientDetails> clientDetailes = drivers.get(0).getClients();
        int pos = 0;
        for (ClientDetails details : clientDetailes ) {
            IClient reverseConnection = mServer.getReverseConnectionForClient(details, new Credentials(null, null));
            assertNotNull("Can't retrieve reverse connection to client", reverseConnection);
            reverseConnection.connect();
            // get chat file for client
            reverseFiles[pos] = new StyxFile(reverseConnection, "/chat");
            outputs[pos] = reverseFiles[pos].openForWriteUnbuffered();
            pos++;
        }

        // lets go
        clientFiles[0].sendMessage();
        syncObject.wait(5000);

        // close server connections
        pos = 0;
        for (ClientDetails details : clientDetailes) {
            IClient reverseConnection = reverseFiles[pos].getIClient();
            outputs[pos].close();
            reverseFiles[pos].close();
            reverseConnection.close();
        }

        // close clients
        for (int i = 0; i < count; i++) {
            clients[i].close();
        }
        Thread.sleep(500);
        clientDetailes = drivers.get(0).getClients();
        assertNotNull(clientDetailes);
        assertEquals(0, clientDetailes.size());
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

    private class ChatStyxFile extends MemoryStyxFile {
        protected String mMessage;
        protected String mMarker;
        protected boolean isCounter;
        protected OutputStream mOut;
        protected StyxFile mFile;

        public ChatStyxFile(String filename, String message, String marker, boolean isCounter) {
            super(filename);
            mMessage = message;
            mMarker = marker;
            this.isCounter = isCounter;

        }

        public void attachToserver(IClient client) throws InterruptedException, StyxException, TimeoutException,
                IOException {
            mFile = new StyxFile(client, "/chat");
            mOut = mFile.openForWriteUnbuffered();
        }

        @Override
        public int write(ClientDetails clientDetails, byte[] data, ULong offset) throws StyxErrorMessageException {
            String message = new String(data, mCharset);
            if ( mMarker.equals(message)) {
                try {
                    sendMessage();
                } catch (IOException e) {
                    StyxErrorMessageException.doException(e.toString());
                }
            }
            return data.length;
        }

        public void sendMessage() throws IOException {
            mOut.write(mMessage.getBytes(mCharset));
        }

        @Override
        public void release() throws IOException {
            super.release();
            mOut.close();
            mFile.close();
        }
    }
}
