V2StyxLib
=========

Java/C#/C++ implementation of Styx (9P2000) protocol


How to use it in Java

Java server:

‘’’
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
        StyxServerManager mServer = new StyxServerManager(InetAddress.getByName(“localhost”),
                PORT,
                false,
                root);
        mServer.start().join();
    }
}
'''

Java client sample:
‘’’
        StyxClientConnection mConnection = new StyxClientConnection(InetAddress.getByName("127.0.0.1"), PORT, false);
        mConnection.connect();
        final StyxFile newFile = new StyxFile(mConnection, FILE_NAME);
        OutputStream output = newFile.openForWrite();
        InputStream input = newFile.openForRead();
        output.write(someData);
        output.flush();
        byte [] remoteHash = new byte[16];
        int read = input.read(remoteHash);
        output.close();
        input.close();
        mConnection.close();
‘’’
