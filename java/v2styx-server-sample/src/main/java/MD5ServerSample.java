import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l6.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.l6.vfs.MemoryStyxFile;
import com.v2soft.styxlib.server.ClientsRepoImpl;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;

import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

/**
 * Java server that calculate MD5 digest.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class MD5ServerSample {
    private static final int PORT = 10234;
    private static final String FILE_NAME = "md5file";

    public static void main(String[] args) throws IOException, InterruptedException, StyxException {
        MemoryStyxFile md5 = new MemoryStyxFile(FILE_NAME){
            protected HashMap<Integer, MessageDigest> mClientsMap = new HashMap<>();
            @Override
            public boolean open(int clientId, int mode)
                    throws StyxException {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    mClientsMap.put(clientId, md);
                } catch (NoSuchAlgorithmException e) {
                    return false;
                }
                return super.open(clientId, mode);
            }
            @Override
            public void close(int clientId) {
                mClientsMap.remove(clientId);
                super.close(clientId);
            }
            @Override
            public int write(int clientId, byte[] data, long offset)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(clientId) ) {
                    mClientsMap.get(clientId).update(data, 0, data.length);
                }
                return super.write(clientId, data, offset);
            }
            @Override
            public int read(int clientId, byte[] outbuffer, long offset, int count)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(clientId) ) {
                    byte[] digest = mClientsMap.get(clientId).digest();
                    if (count < digest.length) {
                        return 0;
                    } else {
                        System.arraycopy(digest, 0, outbuffer, 0, digest.length);
                        return digest.length;
                    }
                }
                return super.read(clientId, outbuffer, offset, count);
            }
        };
        var clientsRepo = new ClientsRepoImpl();
        var driver = new TCPServerChannelDriver(
                InetAddress.getByName("127.0.0.1"),
                PORT,
                false,
                clientsRepo);
        MemoryStyxDirectory root = new MemoryStyxDirectory("root", driver.getSerializer());
        root.addFile(md5);
        StyxServerManager mServer = new StyxServerManager(
                root,
                List.of(driver),
                clientsRepo);
        mServer.start();
        System.out.println("Test server listen on 127.0.0.1:" + PORT);
        mServer.joinThreads();
    }
}
