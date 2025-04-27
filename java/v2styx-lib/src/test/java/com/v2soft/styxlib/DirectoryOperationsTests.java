package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.StyxDeserializerImpl;
import com.v2soft.styxlib.l5.serialization.impl.StyxSerializerImpl;
import com.v2soft.styxlib.l6.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.server.ClientsRepoImpl;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import com.v2soft.styxlib.utils.MetricsAndStats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Client JUnit tests
 *
 * @author V.Shcriabets (vshcryabets@gmail.com)
 */
public class DirectoryOperationsTests {
    private static Logger log = Logger.getLogger(DirectoryOperationsTests.class.getSimpleName());
    private static final int PORT = 10234;
    private Connection mConnection;
    private StyxServerManager mServer;
    private ClientsRepo mClientsRepo = new ClientsRepoImpl();

    @BeforeEach
    public void setUp() throws Exception {
        MetricsAndStats.reset();
        var localHost = InetAddress.getByName("127.0.0.1");
        var testDirectory = new File("./");
        IDataSerializer serializer = new StyxSerializerImpl();
        IDataDeserializer deserializer = new StyxDeserializerImpl();
        TCPChannelDriver.InitConfiguration initConfiguration = new TCPChannelDriver.InitConfiguration(
                serializer,
                deserializer,
                StyxServerManager.DEFAULT_IOUNIT,
                false,
                localHost,
                PORT);

        // Server side
        var serverDriver = new TCPServerChannelDriver(mClientsRepo);
        mServer = new StyxServerManager(new StyxServerManager.Configuration(
                new DiskStyxDirectory(testDirectory, serializer),
                Arrays.asList(serverDriver),
                mClientsRepo,
                serializer,
                deserializer,
                StyxServerManager.DEFAULT_IOUNIT));
        serverDriver.prepare(initConfiguration);
        mServer.start();

        // client side
        var driver = new TCPClientChannelDriver(mClientsRepo);
        var connectionConfiguration = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver,
                mClientsRepo,
                serializer,
                deserializer);
        mConnection = new Connection(connectionConfiguration);
        driver.prepare(initConfiguration);
        assertTrue(mConnection.connect());
    }

    @AfterEach
    public void shutDown() throws InterruptedException, IOException {
        mConnection.close();
        mServer.closeAndWait();
    }

    @Test
    public void testListFilesInRootDir()
            throws IOException, StyxException, InterruptedException, TimeoutException {
        var rootDir = mConnection.getRoot();
        var files = rootDir.listStat();
        assertFalse(files.isEmpty());
        var fileStats = rootDir.listStat();
        assertFalse(fileStats.isEmpty());
    }
}
