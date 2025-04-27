V2StyxLib
=========

Java/C#/C++ implementation of Styx (9P2000) protocol


How to use it in Java

Java server:

```java
public class JavaServerSample {
    private static final int PORT = 10234;
    private static final String FILE_NAME = "md5file";

    public static void main(String[] args) throws IOException, StyxException, InterruptedException, TimeoutException {
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
        MemoryStyxDirectory root = new MemoryStyxDirectory("root");
        root.addFile(md5);
        ClientsRepo clientsRepo = new ClientsRepoImpl();
        IDataSerializer serializer = new StyxSerializerImpl();
        IDataDeserializer deserializer = new StyxDeserializerImpl();
        StyxServerManager.Configuration serverConfiguration;
        TCPChannelDriver.InitConfiguration initConfiguration = new TCPChannelDriver.InitConfiguration(
                serializer,
                deserializer,
                StyxServerManager.DEFAULT_IOUNIT,
                false,
                InetAddress.getLoopbackAddress(),
                PORT);
        var serverDriver = new TCPServerChannelDriver(clientsRepo);
        serverConfiguration = new StyxServerManager.Configuration(
                root,
                Arrays.asList(serverDriver),
                clientsRepo,
                serializer,
                deserializer,
                StyxServerManager.DEFAULT_IOUNIT);
        var server = new StyxServerManager(serverConfiguration);
        serverDriver.prepare(initConfiguration);
        Thread[] threads = server.start();
        System.out.println("Test server listen on 127.0.0.1:" + PORT);
        for(Thread thread : threads) {
            thread.join();
        }
    }
}
```

Java client sample:
```java
    IDataSerializer serializer = new StyxSerializerImpl();
    IDataDeserializer deserializer = new StyxDeserializerImpl();
    ClientsRepo clientsRepo = new ClientsRepoImpl();
    var clientDriver = new TCPClientChannelDriver(clientsRepo);
    clientDriver.prepare(new TCPClientChannelDriver.InitConfiguration(
         serializer,
         deserializer,
         StyxServerManager.DEFAULT_IOUNIT,
         false,
         InetAddress.getLoopbackAddress(),
         PORT
         ));
    
    clientConfiguration = new Connection.Configuration(
        new CredentialsImpl("user", ""),
        clientDriver,
        clientsRepo,
        serializer,
        deserializer);
    var connection = new Connection(clientConfiguration);
    connection.connect();
    StyxFile newFile = connection.open(FILE_NAME);
    OutputStream output = newFile.openForWrite();
    InputStream input = newFile.openForRead();
    output.write(someData);
    output.flush();
    byte [] remoteHash = new byte[16];
    int read = input.read(remoteHash);
    output.close();
    input.close();
    connection.close();
```
