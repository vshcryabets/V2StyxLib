import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.tcp.TCPServerManager;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.ULong;

import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Java server that calculate MD5 digest.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class JavaServerSample {
    private static final int PORT = 10234;
    private static final String FILE_NAME = "md5file";

    public static void main(String[] args) throws IOException, StyxException, InterruptedException, TimeoutException {
        MemoryStyxFile md5 = new MemoryStyxFile(FILE_NAME){
            protected HashMap<ClientState, MessageDigest> mClientsMap = new HashMap<ClientState, MessageDigest>();
            @Override
            public boolean open(ClientState client, int mode)
                    throws IOException {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    mClientsMap.put(client, md);
                } catch (NoSuchAlgorithmException e) {
                    return false;
                }
                return super.open(client, mode);
            }
            @Override
            public void close(ClientState client) {
                mClientsMap.remove(client);
                super.close(client);
            }
            @Override
            public int write(ClientState client, byte[] data, ULong offset)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(client) ) {
                    mClientsMap.get(client).update(data, 0, data.length);
                }
                return super.write(client, data, offset);
            }
            @Override
            public long read(ClientState client, byte[] outbuffer, ULong offset, long count)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(client) ) {
                    byte[] digest = mClientsMap.get(client).digest();
                    if (count < digest.length) {
                        return 0;
                    } else {
                        System.arraycopy(digest, 0, outbuffer, 0, digest.length);
                        return digest.length;
                    }
                }
                return super.read(client, outbuffer, offset, count);
            }
        };
        MemoryStyxDirectory root = new MemoryStyxDirectory("root");
        root.addFile(md5);
        StyxServerManager mServer = new TCPServerManager(InetAddress.getByName("127.0.0.1"),
                PORT,
                false,
                root);
        Thread[] threads = mServer.start();
        for(Thread thread : threads) {
            thread.join();
        }
    }
}