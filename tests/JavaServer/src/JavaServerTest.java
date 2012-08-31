
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.server.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class JavaServerTest {

    public static void main(String[] args) throws IOException, StyxException, InterruptedException, TimeoutException {
        try {
            File testDirectory = new File("./test");
            if ( !testDirectory.exists() ) {
                testDirectory.mkdirs();
            }
            DiskStyxDirectory root = new DiskStyxDirectory(testDirectory);
            MemoryStyxDirectory dir = new MemoryStyxDirectory("vdir1"); 
            root.addFile(dir);
            dir = new MemoryStyxDirectory("vdir2");
            root.addFile(dir);
            dir.addFile(new MemoryStyxFile("vfile1"));
            dir.addFile(new MemoryStyxFile("vfile2"));
            dir.addFile(new MemoryStyxFile("vfile3"));
            dir.addFile(new MemoryStyxFile("vfile4"));
            StyxServerManager manager = new StyxServerManager(
                    InetAddress.getByName("127.0.0.1"), 
                    8080, 
                    false,
                    root,
                    StyxServerManager.PROTOCOL);
            manager.start();
            while ( true ) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}