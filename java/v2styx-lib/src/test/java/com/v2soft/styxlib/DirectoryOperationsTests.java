package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.dev.MetricsAndStats;
import com.v2soft.styxlib.l6.vfs.DiskStyxDirectory;
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
    private StyxSessionDI di = new StyxSessionDIImpl(false);

    @BeforeEach
    public void setUp() throws Exception {
        MetricsAndStats.reset();
        var localHost = InetAddress.getByName("127.0.0.1");
        var testDirectory = new File("./");
        TCPChannelDriver.InitConfiguration initConfiguration = new TCPChannelDriver.InitConfiguration(
                StyxServerManager.DEFAULT_IOUNIT,
                false,
                localHost,
                PORT,
                di);

        // Server side
        var serverDriver = new TCPServerChannelDriver(di);
        mServer = new StyxServerManager(new StyxServerManager.Configuration(
                new DiskStyxDirectory(testDirectory, di),
                Arrays.asList(serverDriver),
                di,
                StyxServerManager.DEFAULT_IOUNIT));
        serverDriver.prepare(initConfiguration);
        mServer.start();

        // client side
        var driver = new TCPClientChannelDriver(di);
        var connectionConfiguration = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                driver,
                di);
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
