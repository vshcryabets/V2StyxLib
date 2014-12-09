package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.StyxFile;
import com.v2soft.styxlib.ConnectionWithExport;
import com.v2soft.styxlib.IClient;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.types.ULong;
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
        ConnectionWithExport connection = new ConnectionWithExport();
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
        ConnectionWithExport connection = new ConnectionWithExport();
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

        ConnectionWithExport connection = new ConnectionWithExport();
        IChannelDriver driver = new TCPClientChannelDriver(
                InetAddress.getByName("127.0.0.1"), PORT,
                false);
        assertTrue(connection.connect(driver));

        ConnectionWithExport connection2 = new ConnectionWithExport();
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
        ConnectionWithExport clients[] = new ConnectionWithExport[count];
        IChannelDriver clientDrivers[] = new TCPClientChannelDriver[count];
        StyxFile reverseFiles[] = new StyxFile[count];
        ChatStyxFile clientFiles[] = new ChatStyxFile[count];
        final OutputStream outputs[] = new OutputStream[count];

        // prepare server chat file
        MemoryStyxFile chatServer = new MemoryStyxFile("chat") {
            @Override
            public int write(ClientDetails clientDetails, final byte[] data, ULong offset) throws StyxErrorMessageException {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String message = new String(data, mCharset);
                        System.out.println(String.format("SERVER GOT %s", message));
                        // sent this message to other clients
//                        for (OutputStream out : outputs) {
//                            try {
//                                out.write(data);
//                            } catch (IOException e) {
//                                System.out.println("ERR "+System.currentTimeMillis());
//                                e.printStackTrace();
//                            }
//                        }
                        System.out.println("END NOTIFY");
                    }
                }).start();
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
            String prefix = "CL" + i;
            clients[i] = new ConnectionWithExport();
            clientDrivers[i] = new TCPClientChannelDriver(
                    InetAddress.getByName("127.0.0.1"), PORT,
                    false);
//            clientDrivers[i].setLogListener(new TestLogListener(prefix));
            String marker = ( i == 0 ? messages[count-1] : messages[i-1]);
            clientFiles[i] = new ChatStyxFile("chat", messages[i], marker, (i == 0), prefix);
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

        Thread.sleep(1000);
        System.out.println("\t\t========================");
        // lets go
        clientFiles[0].sendMessage();
        synchronized (syncObject) {
            syncObject.wait(2500);
        }

        // close server connections
        pos = 0;
        mServer.getDrivers().get(0).setLogListener(new TestLogListener("\tSRV"));
        for (ClientDetails details : clientDetailes) {
            System.out.printf("Trying to disconnect from %s\n", details.toString());
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

    private class ChatStyxFile extends MemoryStyxFile {
        protected String mMessage;
        protected String mMarker;
        protected boolean isCounter;
        protected OutputStream mOut;
        protected StyxFile mFile;
        protected String mPrefix;

        public ChatStyxFile(String filename, String message, String marker, boolean isCounter, String prefix) {
            super(filename);
            mMessage = message;
            mMarker = marker;
            this.isCounter = isCounter;
            mPrefix = prefix;
        }

        public void attachToserver(IClient client) throws InterruptedException, StyxException, TimeoutException,
                IOException {
            mFile = new StyxFile(client, "/chat");
            mOut = mFile.openForWriteUnbuffered();
        }

        @Override
        public int write(ClientDetails clientDetails, byte[] data, ULong offset) throws StyxErrorMessageException {
            String message = new String(data, mCharset);
            System.out.println(String.format("%s GOT %s", mPrefix, message));
//            if ( mMarker.equals(message)) {
//                try {
//                    sendMessage();
//                } catch (IOException e) {
//                    StyxErrorMessageException.doException(e.toString());
//                }
//            }
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
