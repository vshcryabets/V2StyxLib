package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.l6.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.StyxServerManager;
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
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Client JUnit tests
 * @author V.Shcriabets (vshcryabets@gmail.com)
 */
public class DirectoryOperationsTests {
    private static Logger log = Logger.getLogger(DirectoryOperationsTests.class.getSimpleName());
    private static final int PORT = 10234;
    private Connection mConnection;
    private StyxServerManager mServer;

    @BeforeEach
    public void setUp() throws Exception {
        MetricsAndStats.reset();
        var localHost = InetAddress.getByName("127.0.0.1");
        var testDirectory = new File("./");
        var serverDriver = new TCPServerChannelDriver(localHost, PORT, false);
        mServer = new StyxServerManager(
                new DiskStyxDirectory(testDirectory, serverDriver.getSerializer()),
                Collections.singletonList(serverDriver));
        mServer.start();
        var driver = new TCPClientChannelDriver(localHost, PORT, false);
        mConnection = new Connection(new CredentialsImpl("user", ""), driver);
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
