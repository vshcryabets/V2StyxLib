import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.l5.serialization.impl.MessageSerializerImpl;
import com.v2soft.styxlib.l6.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.l6.vfs.MemoryStyxFile;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;

import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Java server that calculate MD5 digest.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class MD5ServerSample {
    private static final int PORT = 10234;
    private static final String FILE_NAME = "md5file";

    public static void main(String[] args) throws IOException, InterruptedException {
        MemoryStyxFile md5 = new MemoryStyxFile(FILE_NAME){
            protected HashMap<ClientDetails, MessageDigest> mClientsMap = new HashMap<ClientDetails, MessageDigest>();
            @Override
            public boolean open(ClientDetails client, int mode)
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
            public void close(ClientDetails client) {
                mClientsMap.remove(client);
                super.close(client);
            }
            @Override
            public int write(ClientDetails client, byte[] data, long offset)
                    throws StyxErrorMessageException {
                if ( mClientsMap.containsKey(client) ) {
                    mClientsMap.get(client).update(data, 0, data.length);
                }
                return super.write(client, data, offset);
            }
            @Override
            public long read(ClientDetails client, byte[] outbuffer, long offset, long count)
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
        var driver = new TCPServerChannelDriver(
                InetAddress.getByName("127.0.0.1"),
                PORT,
                false);
        MemoryStyxDirectory root = new MemoryStyxDirectory("root", driver.getSerializer());
        root.addFile(md5);
        StyxServerManager mServer = new StyxServerManager(
                root,
                Arrays.asList(driver));
        mServer.start();
        System.out.println("Test server listen on 127.0.0.1:" + PORT);
        mServer.joinThreads();
    }
}
