

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.exceptions.StyxException;

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
        	testBuffer();
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
            StyxServerManager manager = new StyxServerManager(InetAddress.getLocalHost(), 8080, false);
            manager.start();
            while ( true ) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testBuffer() {
    	byte [] out = new byte[8];
    	ByteBuffer primaryBuffer = ByteBuffer.allocateDirect(19);
    	int lastEnd = 0;
    	
    	for ( int i = 0; i < 30; i++) {
    		// write
    		byte [] buffer = new byte[]{(byte) (i*7), (byte) (i*7+1), (byte) (i*7+2), (byte) (i*7+3), (byte) (i*7+4), (byte) (i*7+5), (byte) (i*7+6)};
    		int remaining = primaryBuffer.remaining(); 
    		if ( remaining < buffer.length ) {
    			// split write
    			primaryBuffer.put(buffer, 0, remaining);
    			primaryBuffer.position(0);
    			primaryBuffer.limit(lastEnd);
    			primaryBuffer.put(buffer, remaining, buffer.length-remaining);
    		} else {
    			primaryBuffer.put(buffer);
    		}
    		// read
    		int inBuffer = primaryBuffer.position()-lastEnd;
    		if ( inBuffer < 0 ) {
    			// split read
    			int newEnd = primaryBuffer.position();
    			primaryBuffer.limit(16);
    			primaryBuffer.position(lastEnd);
    			int rest = primaryBuffer.remaining();
    			primaryBuffer.get(out, 0, rest);
    			primaryBuffer.position(0);
    			primaryBuffer.get(out, rest, out.length-rest);
    			lastEnd = primaryBuffer.position();
    			primaryBuffer.position(newEnd);
    			for ( int q = 0; q < 8; q++ ) System.out.print((out[q]&0xFF)+" ");
    		} else if ( inBuffer >= 8 ) {
    			int newEnd = primaryBuffer.position();
    			primaryBuffer.position(lastEnd);
    	    	primaryBuffer.get(out, 0, 8);
    	    	lastEnd = primaryBuffer.position();
    	    	primaryBuffer.position(newEnd);
    	    	for ( int q = 0; q < 8; q++ ) System.out.print((out[q]&0xFF)+" ");
    		}
    	}
		int inBuffer = primaryBuffer.position()-lastEnd;
		if ( inBuffer < 0 ) {
			int newEnd = primaryBuffer.position();
			primaryBuffer.limit(16);
			primaryBuffer.position(lastEnd);
			int rest = primaryBuffer.remaining();
			primaryBuffer.get(out, 0, rest);
			primaryBuffer.position(0);
			primaryBuffer.get(out, rest, out.length-rest);
			lastEnd = primaryBuffer.position();
			primaryBuffer.position(newEnd);
			for ( int q = 0; q < 8; q++ ) System.out.print((out[q]&0xFF)+" ");
		} else if ( inBuffer > 0 ) {
			int newEnd = primaryBuffer.position();
			primaryBuffer.position(lastEnd);
	    	primaryBuffer.get(out, 0, inBuffer);
	    	for ( int q = 0; q < inBuffer; q++ ) System.out.print((out[q]&0xFF)+" ");
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