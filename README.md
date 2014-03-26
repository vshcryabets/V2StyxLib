V2StyxLib
=========

Java/C#/C++ implementation of Styx (9P2000) protocol


How to use it in Java

Java server:

'''
java
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
            int port = 10234;
            StyxServerManager manager = new StyxServerManager(InetAddress.getByName("127.0.0.1"),
                    port,
                    false,
                    root,
                    StyxServerManager.PROTOCOL);
            manager.start().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
'''

Java client sample:
