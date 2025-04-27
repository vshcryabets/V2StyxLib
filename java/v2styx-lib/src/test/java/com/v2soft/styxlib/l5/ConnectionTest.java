package com.v2soft.styxlib.l5;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.EmptyMessagesProcessor;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.serialization.impl.StyxDeserializerImpl;
import com.v2soft.styxlib.l5.serialization.impl.StyxSerializerImpl;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.l6.io.StyxFileBufferedInputStream;
import com.v2soft.styxlib.l6.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.l6.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.server.ClientsRepoImpl;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import com.v2soft.styxlib.utils.MetricsAndStats;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Client JUnit tests
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class ConnectionTest {
    private static Logger log = Logger.getLogger(ConnectionTest.class.getSimpleName());
    private static final int PORT = 10234;
    private Connection mConnection;
    private StyxServerManager mServer;
    private ClientsRepo mClientsRepo = new ClientsRepoImpl();
    private IDataSerializer serializer = new StyxSerializerImpl();
    private IDataDeserializer deserializer = new StyxDeserializerImpl();
    private Connection.Configuration clientConfiguration;
    private StyxServerManager.Configuration serverConfiguration;
    private TCPChannelDriver.InitConfiguration initConfiguration = new TCPServerChannelDriver.InitConfiguration(
            serializer,
            deserializer,
            StyxServerManager.DEFAULT_IOUNIT,
            false,
            InetAddress.getLoopbackAddress(),
            PORT
    );

    @BeforeEach
    public void setUp() throws Exception {
        MetricsAndStats.reset();
        var testDirectory = new File("./test");
        if (!testDirectory.exists()) {
            testDirectory.mkdirs();
        }
        var serverDriver = new TCPServerChannelDriver(mClientsRepo);
        serverDriver.prepare(initConfiguration);
        serverConfiguration = new StyxServerManager.Configuration(
                new DiskStyxDirectory(testDirectory, serializer),
                Arrays.asList(serverDriver),
                mClientsRepo,
                serializer,
                deserializer,
                StyxServerManager.DEFAULT_IOUNIT);
        mServer = new StyxServerManager(serverConfiguration);
        mServer.start();
        var clientDriver = new TCPClientChannelDriver(mClientsRepo);
        clientDriver.prepare(initConfiguration);
        clientConfiguration = new Connection.Configuration(
                new CredentialsImpl("user", ""),
                clientDriver,
                mClientsRepo,
                serializer,
                deserializer);
        mConnection = new Connection(clientConfiguration);
        assertTrue(mConnection.connect());
    }

    @AfterEach
    public void shutDown() throws InterruptedException, IOException {
        mConnection.close();
        mServer.closeAndWait();
    }

    // TVersion & TAttach
    @Test
    @Tag("dev")
    public void testConnection() throws IOException, InterruptedException, TimeoutException {
        int count = 1000;
        clientConfiguration.transmitter.clearStatistics();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            mConnection.sendVersionMessage();
        }
        assertEquals(count * 3, clientConfiguration.transmitter.getTransmittedCount()); // TVersion, Tattach, TClunk
        assertEquals(0, clientConfiguration.transmitter.getErrorsCount());
        long diff = System.currentTimeMillis() - startTime;
        log.info(String.format("\tTransmited %d messages\n\t" +
                        //"Received %d messages\n\t" +
                        "Error %d messages\n\t" +
                        "Average time for connection %f ms",
                clientConfiguration.transmitter.getTransmittedCount(),
                clientConfiguration.transmitter.getErrorsCount(),
                (float)diff / (float)count
        ));
    }

    // TVersion, Tattach, Twalk, create, write, Tclunk, open, read, remove
    @Test
    public void testFileCreation() throws IOException {
        final StyxFile newFile = mConnection.open(UUID.randomUUID().toString());
        newFile.create(FileMode.ReadOthersPermission | FileMode.WriteOthersPermission);
        final OutputStream out = newFile.openForWrite();
        assertNotNull(out);
        byte[] testArray = new byte[]{1, 3, 5, 7, 11, 13, 17, 19, 23, 29};
        out.write(testArray);
        out.close();
        final InputStream in = newFile.openForRead();
        assertNotNull(in);
        final byte[] readArray = new byte[testArray.length];
        int read = in.read(readArray);
        assertEquals(testArray.length, read);
        in.close();
        assertArrayEquals(testArray, readArray);
        newFile.delete();
        newFile.close();
    }

    @Test
    public void testFSTree() throws IOException {
        // create 2 directory and 4 sub-directories
        String nameA = UUID.randomUUID().toString();
        String nameB = UUID.randomUUID().toString();
        String nameAA = nameA + StyxFile.SEPARATOR + UUID.randomUUID().toString();
        String nameAB = nameA + StyxFile.SEPARATOR + UUID.randomUUID().toString();
        String nameBA = UUID.randomUUID().toString();
        String nameBB = UUID.randomUUID().toString();
        String nameBC = nameB + StyxFile.SEPARATOR + UUID.randomUUID().toString();

        StyxFile dirBC = null;
        try {
            dirBC = mConnection.open(nameBC);
            dirBC.create(FileMode.ReadOthersPermission |
                    FileMode.WriteOthersPermission |
                    FileMode.Directory);
        } catch (StyxErrorMessageException e) {
            dirBC = null; // we should get an error
        }
        assertNull(dirBC);


        final StyxFile dirA = mConnection.open(nameA);
        final StyxFile dirB = mConnection.open(nameB);
        final StyxFile dirAA = mConnection.open(nameAA);
        final StyxFile dirAB = mConnection.open(nameAB);


        dirA.create(FileMode.PERMISSION_BITMASK | FileMode.Directory);
        dirB.create(FileMode.PERMISSION_BITMASK | FileMode.Directory);
        dirAA.create(FileMode.PERMISSION_BITMASK | FileMode.Directory);
        dirAB.create(FileMode.PERMISSION_BITMASK | FileMode.Directory);
        // test other way to create file (with specified parent)
        final StyxFile dirBA = dirB.walk(nameBA);
        final StyxFile dirBB = dirB.walk(nameBB);
        dirBA.create(FileMode.PERMISSION_BITMASK | FileMode.Directory);
        dirBB.create(FileMode.PERMISSION_BITMASK | FileMode.Directory);

        try {
            dirA.delete();
            assertTrue(false, "Delete should return an error");
        } catch (StyxErrorMessageException e) {
        }
        // we lost FID, restore it
        dirA.getFID();

        dirBB.delete();
        dirBB.close();
        dirBA.delete();
        dirBA.close();

        dirAB.delete();
        dirAB.close();
        dirAA.delete();
        dirAA.close();

        dirA.delete();
        dirA.close();
        dirB.delete();
        dirB.close();
    }

    @Test
    public void testBigFileTransmition()
            throws IOException {
        byte[] buffer = new byte[156];
        System.out.println("Generating pattern...");
        Random random = new Random();
        random.nextBytes(buffer);
        System.out.println("Count CRC32...");
        final CRC32 crcounter = new CRC32();
        crcounter.update(buffer);
        long crc32 = crcounter.getValue();
        long startTime = System.currentTimeMillis();
        long filessize = 200 * 1024 * 1024;
        System.out.println("Copy file to server...");

        // create file
        String filename = UUID.randomUUID().toString();
        StyxFile file = mConnection.open(filename);
        file.create(FileMode.PERMISSION_BITMASK);

        final OutputStream out = file.openForWrite();
        assertNotNull(out);
        // write it
        long bufcount = filessize / buffer.length;
        long last = filessize % buffer.length;
        for (int j = 0; j < bufcount; j++) {
            out.write(buffer);
        }
        out.write(buffer, 0, (int) last);
        // close it
        out.close();
        file.close();

        long writeTime = System.currentTimeMillis();
        System.out.println("Read from server...");

        InputStream in = file.openForRead();
        assertNotNull(in);
        // read it
        bufcount = filessize / buffer.length;
        //            long last = filessize%buffer.length;
        int readed = 0;
        for (int j = 0; j < bufcount; j++) {
            readed = 0;
            while (readed < buffer.length) {
                readed += in.read(buffer, readed, buffer.length - readed);
            }
            crcounter.reset();
            crcounter.update(buffer, 0, readed);
            assertEquals(crc32, crcounter.getValue());
        }

        // close it
        in.close();

        // delete it
        file.delete();

        long diff = System.currentTimeMillis() - writeTime;
        System.out.println(String.format("Write done in %d ms", (writeTime - startTime)));
        System.out.println(String.format("Read done in %d ms", diff));
        System.out.println(String.format("\tTransmitted %d messages", clientConfiguration.transmitter.getTransmittedCount()));
//        System.out.println(String.format("\tReceived %d messages", mConnection.mTransmitter.getReceivedCount()));
        System.out.println(String.format("\tError %d messages", clientConfiguration.transmitter.getErrorsCount()));
        //        System.out.println(String.format("\tAverage time for connection %d ms",diff/count));
    }

    @Test
    public void testWriteTransmitionSpeed()
            throws IOException {
        int blockSize = 128;
        long blocksCount = 1024 * 1024;
        final String filename = "write";
        final long[] stat = new long[1];
        ((DiskStyxDirectory) serverConfiguration.root).addFile(new MemoryStyxFile(filename) {
            @Override
            public int write(int clientId, byte[] data, long offset) {
                stat[0] += data.length;
                return data.length;
            }
        });
        byte[] buffer = new byte[blockSize];
        System.out.println("Generating pattern...");
        Random random = new Random();
        random.nextBytes(buffer);

        StyxFile file = mConnection.open(filename);
        OutputStream out = file.openForWriteUnbuffered();
        long startTime = System.nanoTime();
        // write it
        for (int j = 0; j < blocksCount; j++) {
            out.write(buffer);
        }
        long endTime = System.nanoTime();
        // close it
        out.close();
        file.close();

        long writeTimeMs = (endTime - startTime) / 1000000;

        Assertions.assertEquals(blockSize * blocksCount, stat[0], "Write and received size not equals");
        System.out.println(String.format("\tServer received %d bytes", stat[0]));
        System.out.println(String.format("\tWrite done in %d ms", writeTimeMs));
        System.out.println(String.format("\tTransmited %d messages", clientConfiguration.transmitter.getTransmittedCount()));
        System.out.println(String.format("\tError %d messages", clientConfiguration.transmitter.getErrorsCount()));
        System.out.println(String.format("\tByteBuffer allocations count %d", MetricsAndStats.byteBufferAllocation));
        System.out.println(String.format("\tbyte[] allocations count %d", MetricsAndStats.byteArrayAllocation));
        System.out.println(String.format("\tbyte[] allocations count RRead %d", MetricsAndStats.byteArrayAllocationRRead));
        System.out.println(String.format("\tbyte[] allocations count TWrite %d", MetricsAndStats.byteArrayAllocationTWrite));
        System.out.println(String.format("\tbyte[] allocations count IO %d",
                MetricsAndStats.byteArrayAllocationIo));
    }

    // TVersion, Tattach, Twalk, create, write, Tclunk, open, read, remove
    @Test
    public void testFileSeek() throws IOException, StyxException, InterruptedException, TimeoutException {
        final StyxFile newFile = mConnection.open(UUID.randomUUID().toString());
        newFile.create(FileMode.ReadOthersPermission |
                FileMode.WriteOthersPermission);
        final OutputStream out = newFile.openForWrite();
        assertNotNull(out);
        // prepare random block
        int fullSize = 4096;
        byte[] randomBlock = new byte[fullSize];
        final Random rnd = new Random();
        for (int i = 0; i < fullSize; i++) {
            randomBlock[i] = (byte) (rnd.nextInt(256) - 128);
        }
        // write it to file
        out.write(randomBlock);
        out.close();
        final StyxFileBufferedInputStream in = newFile.openForRead();
        assertNotNull(in);
        // test partial reads
        int testCount = 10;
        for (int i = 0; i < testCount; i++) {
            int position = rnd.nextInt(fullSize * 8 / 10);
            int maxRead = fullSize - position;
            int wantRead = rnd.nextInt(maxRead);
            final byte[] readArray = new byte[wantRead];
            in.seek(position);
            int read = 0;
            while (read < wantRead) {
                read += in.read(readArray, read, wantRead - read);
            }
            assertEquals(wantRead, read);
            final byte[] sampleArray = new byte[wantRead];
            System.arraycopy(randomBlock, position, sampleArray, 0, wantRead);
            assertArrayEquals(sampleArray, readArray);
        }
        in.close();
        newFile.delete();
        newFile.close();
    }
}
