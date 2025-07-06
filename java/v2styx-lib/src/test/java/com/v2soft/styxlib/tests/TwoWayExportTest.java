package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.ConnectionWithExport;
import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.IClient;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.l6.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.l6.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPDualLinkServerManager;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import com.v2soft.styxlib.utils.OwnDI;
import com.v2soft.styxlib.utils.OwnDIImpl;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client JUnit tests
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class TwoWayExportTest {
    private static final int PORT = 10234;
    private TCPDualLinkServerManager mServer;
    private final Charset mCharset = StandardCharsets.UTF_8;
    private OwnDI di = new OwnDIImpl();
    private StyxServerManager.Configuration serverConfiguration;
    private TCPServerChannelDriver mServerDriver;
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

    private void startServer() throws IOException, StyxException {
        MemoryStyxFile md5 = new MD5StyxFile(di);
        mServerDriver = new TCPServerChannelDriver(di.getClientsRepo());
        MemoryStyxDirectory root = new MemoryStyxDirectory("root", di);
        root.addFile(md5);
        serverConfiguration = new StyxServerManager.Configuration(
                root,
                Arrays.asList(mServerDriver),
                di,
                StyxServerManager.DEFAULT_IOUNIT);
        mServer = new TCPDualLinkServerManager(serverConfiguration);
        mServerDriver.prepare(initConfiguration);
        mServer.start();
    }

    // TVersion & TAttach
    @Test
    @Disabled
    public void testTwoWayExport() throws IOException, StyxException, InterruptedException, TimeoutException, NoSuchAlgorithmException {
        var driver = new TCPClientChannelDriver(di.getClientsRepo());
        MemoryStyxFile md5 = new MD5StyxFile(di);
        MemoryStyxDirectory root = new MemoryStyxDirectory("clientroot", di);
        root.addFile(md5);
        var clientConfiguration = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver,
                di);
        driver.prepare(initConfiguration);
        ConnectionWithExport connection = new ConnectionWithExport(clientConfiguration);
        connection.export(root);

        Assertions.assertTrue(connection.connect());
        ClientServerTest.checkMD5Hash(connection);
        ClientServerTest.checkMD5Hash(connection);
        ClientServerTest.checkMD5Hash(connection);

        // reverse test
        Collection<Integer> clientDetailses = mServerDriver.getClients();
        Integer clientDetails = clientDetailses.iterator().next();
        IClient reverseConnection = mServer.getReverseConnectionForClient(clientDetails,
                new CredentialsImpl(null, null));
        Assertions.assertNotNull(reverseConnection, "Can't retrieve reverse connection to client");
        reverseConnection.connect();
        ClientServerTest.checkMD5Hash(reverseConnection);
        ClientServerTest.checkMD5Hash(reverseConnection);
        ClientServerTest.checkMD5Hash(reverseConnection);

        reverseConnection.close();

        ClientServerTest.checkMD5Hash(connection);
        ClientServerTest.checkMD5Hash(connection);
        ClientServerTest.checkMD5Hash(connection);

        connection.close();
    }

    @Test
    public void testGetClientsFromClient() throws IOException, InterruptedException, TimeoutException, StyxException {
        var driver = new TCPClientChannelDriver(di.getClientsRepo());
        var clientConfiguration = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver,
                di);
        driver.prepare(initConfiguration);
        ConnectionWithExport connection = new ConnectionWithExport(clientConfiguration);
        Assertions.assertTrue(connection.connect());
        Collection<Integer> clientDetailses = driver.getClients();
        Assertions.assertNotNull(clientDetailses);
        Assertions.assertEquals(1, clientDetailses.size());
        Integer clientId = clientDetailses.iterator().next();
        var pseudoClientDetails = di.getClientsRepo().getClient(clientId);
        Assertions.assertNotNull(pseudoClientDetails);
        Assertions.assertNotNull(pseudoClientDetails.getDriver());
        Assertions.assertEquals(driver, pseudoClientDetails.getDriver());
        connection.close();
    }

    @Test
    public void testGetClientsFromServer() throws IOException, InterruptedException, TimeoutException, StyxException {
        var driver = new TCPClientChannelDriver(di.getClientsRepo());
        var clientConfiguration = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver,
                di);
        driver.prepare(initConfiguration);
        ConnectionWithExport connection = new ConnectionWithExport(clientConfiguration);
        Assertions.assertTrue(connection.connect());

        var driver2 = new TCPClientChannelDriver(di.getClientsRepo());
        var clientConfiguration2 = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver2,
                di);
        driver2.prepare(initConfiguration);
        ConnectionWithExport connection2 = new ConnectionWithExport(clientConfiguration2);
        Assertions.assertTrue(connection2.connect());

        Collection<Integer> clientDetailses = mServerDriver.getClients();
        Assertions.assertNotNull(clientDetailses);
        Assertions.assertEquals(2, clientDetailses.size());

        connection2.close();
        Thread.sleep(500);
        clientDetailses = mServerDriver.getClients();
        Assertions.assertNotNull(clientDetailses);
        Assertions.assertEquals(1, clientDetailses.size());

        connection.close();
        Thread.sleep(500);
        clientDetailses = mServerDriver.getClients();
        Assertions.assertNotNull(clientDetailses);
        Assertions.assertEquals(0, clientDetailses.size());
    }

//    @Test
//    @Disabled
//    public void testChat() throws IOException, InterruptedException, TimeoutException, StyxException {
//        int count = 5;
//        final AtomicInteger syncObject = new AtomicInteger(0);
//
//        String[] messages = new String[count];
//        ConnectionWithExport[] clients = new ConnectionWithExport[count];
//        IChannelDriver[] clientDrivers = new TCPClientChannelDriver[count];
//        ChatStyxFile[] clientFiles = new ChatStyxFile[count];
//        final OutputStream[] outputs = new OutputStream[count];
//
//        // prepare server chat file
//        MemoryStyxFile chatServer = new ChatServerFile(outputs);
//        ((MemoryStyxDirectory) mServer.getRoot()).addFile(chatServer);
//
//        // prepare messages
//        for (int i = 0; i < count; i++) {
//            messages[i] = UUID.randomUUID().toString();
//        }
//        // create clients
//        for (int i = 0; i < count; i++) {
//            String prefix = "CL" + i;
//            var driver = new TCPClientChannelDriver(InetAddress.getByName("127.0.0.1"), PORT, false);
//            clientDrivers[i] = driver;
//            clients[i] = new ConnectionWithExport(new CredentialsImpl("user", ""), clientDrivers[i]);
////            clientDrivers[i].setLogListener(new TestLogListener(prefix));
//            String marker = (i == 0 ? messages[count - 1] : messages[i - 1]);
//            clientFiles[i] = new ChatStyxFile("chat", messages[i], marker, (i == 0 ? syncObject : null), prefix);
//            MemoryStyxDirectory root = new MemoryStyxDirectory("clientroot",
//                    driver.getSerializer());
//            root.addFile(clientFiles[i]);
//            clients[i].export(root);
//            Assertions.assertTrue(clients[i].connect());
//            clientFiles[i].attachToServer(clients[i]);
//        }
//
//        // prepare server
//        List<IChannelDriver> drivers = mServer.getDrivers();
//        Collection<ClientDetails> clientDetails = drivers.get(0).getClients();
//        int pos = 0;
//        for (ClientDetails details : clientDetails) {
//            IClient reverseConnection = mServer.getReverseConnectionForClient(details,
//                    new CredentialsImpl(null, null));
//            Assertions.assertNotNull(reverseConnection,"Can't retrieve reverse connection to client");
//            reverseConnection.connect();
//            // get chat file for client
//            outputs[pos] = new StyxUnbufferedFileOutputStream(reverseConnection, "/chat");
//            pos++;
//        }
//
//        Thread.sleep(1000);
//        System.out.println("\t\t========================");
//        // lets go
//        clientFiles[0].sendMessage();
//        synchronized (syncObject) {
//            syncObject.wait(5000);
//        }
//        Assertions.assertTrue(syncObject.get() >= 100);
//
//        chatServer.release();
//
//        // close server connections
//        pos = 0;
//        for (ClientDetails details : clientDetails) {
//            System.out.printf("Trying to disconnect from %s\n", details.toString());
//            IClient reverseConnection = ((StyxUnbufferedFileOutputStream) outputs[pos]).getIClient();
//            outputs[pos].close();
//            reverseConnection.close();
//            pos++;
//        }
//
//        // close clients
//        for (int i = 0; i < count; i++) {
//            clients[i].close();
//        }
//        Thread.sleep(500);
//        clientDetails = drivers.get(0).getClients();
//        Assertions.assertNotNull(clientDetails);
//        Assertions.assertEquals(0, clientDetails.size());
//    }

    private class ChatServerFile extends MemoryStyxFile {
        final OutputStream mOutputs[];
        protected LinkedBlockingQueue<String> mQueue;
        protected Thread mWorker;
        protected boolean isWorking;

        public ChatServerFile(OutputStream[] outputs, OwnDI di) {
            super("chat", di);
            mOutputs = outputs;
            mQueue = new LinkedBlockingQueue<String>();
            mWorker = new Thread(mRunnable);
            mWorker.start();
        }

        @Override
        public int write(int cleintId, final byte[] data,
                         long offset) throws StyxErrorMessageException {
            String message = new String(data, mCharset);
            System.out.println(String.format("%d SERVER GOT %s", System.currentTimeMillis(), message));
            mQueue.offer(message);
            return data.length;
        }

        private final Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                isWorking = true;
                while (isWorking) {
                    try {
                        String value = mQueue.poll(10, TimeUnit.SECONDS);
                        if (value == null) {
                            continue;
                        }
                        // sent this message to other clients
                        for (OutputStream out : mOutputs) {
                            try {
                                out.write(value.getBytes(mCharset));
                            } catch (IOException e) {
                                System.err.println("ERR " + System.currentTimeMillis());
                                e.printStackTrace();
                                Assertions.assertTrue(false, e.toString());
                            }
                        }
                        System.out.println(System.currentTimeMillis() + " END NOTIFY");
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        @Override
        public void release() throws IOException {
            super.release();
            if (mWorker != null) {
                isWorking = false;
                try {
                    mWorker.join(3000);
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
                mWorker.interrupt();
                mWorker = null;
            }
        }
    }

    private class ChatStyxFile extends MemoryStyxFile {
        protected String mMessage;
        protected String mMarker;
        protected OutputStream mOut;
        protected StyxFile mFile;
        protected String mPrefix;
        protected final AtomicInteger mSyncObject;

        public ChatStyxFile(String filename,
                            String message,
                            String marker,
                            AtomicInteger syncObject,
                            String prefix,
                            OwnDI di) {
            super(filename, di);
            mMessage = message;
            mMarker = marker;
            mSyncObject = syncObject;
            mPrefix = prefix;
        }

        public void attachToServer(IClient client) throws InterruptedException, StyxException, TimeoutException,
                IOException {
            mFile = client.open("/chat");
            mOut = mFile.openForWriteUnbuffered();
        }

        @Override
        public int write(int clientId, byte[] data,
                         long offset) throws StyxErrorMessageException {
            String message = new String(data, mCharset);
            System.out.println(String.format("%s GOT %s", mPrefix, message));
            if (mMarker.equals(message)) {
                if (mSyncObject != null) {
                    synchronized (mSyncObject) {
                        int value = mSyncObject.incrementAndGet();
                        if (value >= 100) {
                            mSyncObject.notifyAll();
                        }
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
                try {
                    sendMessage();
                } catch (IOException e) {
                    throw StyxErrorMessageException.newInstance(e.toString());
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
