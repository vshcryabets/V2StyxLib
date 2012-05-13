

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class JavaServerTest {

    private static final String TEST_TVERSION = "tversion";
    private static final String TEST_BIGFILES = "bigfiles";
    private static final String PARAM_HOST = "-h";
    private static final String PARAM_PORT = "-p";
    private static final String PARAM_COUNT = "-c";
    private static final String PARAM_SIZE = "-s";

    public static void main(String[] args) throws IOException, StyxException, InterruptedException, TimeoutException {
        try {
//            if ( args.length == 0 ) {
//                showUsage();
//                return;
//            }
            int pos = 0;
            String host = "localhost";
            int port = 8080, count = 100;
            long size = 1024L*1024L*1024L*2; // 2GB
            while ( pos < args.length ) {
                String command = args[pos];
            /*    if ( command.equalsIgnoreCase(PARAM_HOST)) {
                    pos++;
                    host = args[pos];
                } else if ( command.equalsIgnoreCase(PARAM_PORT)) {
                    pos++;
                    port = Integer.parseInt(args[pos]);
                } else if ( command.equalsIgnoreCase(PARAM_SIZE)) {
                    pos++;
                    size = Long.parseLong(args[pos]);
                } else if ( command.equalsIgnoreCase(PARAM_COUNT)) {
                    pos++;
                    count = Integer.parseInt(args[pos]);
                } else if ( command.equalsIgnoreCase(TEST_TVERSION)) {
                    TVersionTest test = new TVersionTest(host, port);
                    test.start(count);
                } else if ( command.equalsIgnoreCase(TEST_BIGFILES)) {
                    BigFiles test = new BigFiles(host, port);
                    test.start(size);
                }*/
                pos++;
            }
            MemoryStyxDirectory root = new MemoryStyxDirectory("memory");
            root.addFile(new MemoryStyxDirectory("dir1"));
            root.addFile(new MemoryStyxDirectory("dir2"));
            root.addFile(new MemoryStyxDirectory("dir3"));
            root.addFile(new MemoryStyxDirectory("dir4"));
            StyxServerManager manager = new StyxServerManager(InetAddress.getLocalHost(), 
            		8080, 
            		false,
            		root);
            manager.start();
            while ( true ) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("\tJavaServerTest -p serverport -s sizeinbytes TESTNAME\n");
        System.out.println("\tTests:\n");
        System.out.println("\tTVERSION - program will test connection to server. In this mode only TVersion messages will be send to server");
        System.out.println("\t"+TEST_BIGFILES+" - program will test connection to server. In this mode program will create few big files and will write pattern data into it.");
    }
}