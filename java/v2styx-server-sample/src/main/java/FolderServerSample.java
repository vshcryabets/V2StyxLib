import com.v2soft.styxlib.l6.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Java server that share current directory.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class FolderServerSample {
    private static final int PORT = 10234;

    public static void main(String[] args) throws IOException, InterruptedException {
        var root = new DiskStyxDirectory(new File("."));
        //
        var mServer = new StyxServerManager(
                root,
                Arrays.asList(new TCPServerChannelDriver(
                        InetAddress.getByName("127.0.0.1"),
                        PORT,
                        false))
        );
        mServer.start();
        System.out.println("Test server listen on 127.0.0.1:" + PORT);
        mServer.joinThreads();
    }
}
