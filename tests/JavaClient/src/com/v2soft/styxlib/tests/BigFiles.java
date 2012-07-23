package com.v2soft.styxlib.tests;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.zip.CRC32;

import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.messages.base.enums.FileMode;

/**
 * Big files transfer test
 * @author vshcryabets@gmail.com
 *
 */
public class BigFiles {
    private StyxClientConnection mManager;

    public BigFiles(String server, int port) throws UnknownHostException {
        mManager = new  StyxClientConnection(InetAddress.getByName(server),
                port, null);
    }

    public void start(long size) throws Exception {
        mManager.connect();
        int filecount = 10;
        byte [] buffer = new byte[156];
        System.out.println("Generating pattern...");
        Random random = new Random();
        random.nextBytes(buffer);
        System.out.println("Count CRC32...");
        CRC32 crcounter = new CRC32();
        crcounter.update(buffer);
        long crc32 = crcounter.getValue();
        long startTime = System.currentTimeMillis();
        long filessize=size/filecount;
        System.out.println("Copy files to server...");
        for ( int i = 0 ; i < filecount; i++) {

            // create file
            String filename = "bigfile"+i;
            StyxFile file = new StyxFile(mManager, filename);
            OutputStream out = file.create(FileMode.PERMISSION_BITMASK);

            // write it
            long bufcount = filessize/buffer.length;
            long last = filessize%buffer.length;
            for ( int j = 0 ; j < bufcount; j++ ) {
                out.write(buffer);
            }
            out.write(buffer, 0, (int)last);

            // close it
            out.close();
        }
        long writeTime = System.currentTimeMillis();
        System.out.println("Read from server...");
        for ( int i = 0 ; i < filecount; i++) {

            // open file
            String filename = "bigfile"+i;
            StyxFile file = new StyxFile(mManager, filename);
            InputStream in = file.openForRead();

            // read it
            long bufcount = filessize/buffer.length;
            //            long last = filessize%buffer.length;
            int readed = 0;
            for ( int j = 0 ; j < bufcount; j++ ) {
                readed = 0;
                while ( readed < buffer.length ) {
                    readed += in.read(buffer, readed, buffer.length-readed);
                }
                crcounter.reset();
                crcounter.update(buffer, 0, readed);
                if ( crcounter.getValue() != crc32 )
                    throw new Exception("CRC32 not equals buf#"+j+" file#"+i+" bufferSize="+readed);
            }

            // close it
            in.close();

            // delete it
            file.delete();
        }

        long diff = System.currentTimeMillis()-writeTime;
        System.out.println(String.format("Write done in %d ms", (writeTime-startTime)));
        System.out.println(String.format("Read done in %d ms", diff));
//        System.out.println(String.format("\tTransmited %d messages", mManager.getMessenger().getTransmitedCount()));
//        System.out.println(String.format("\tReceived %d messages", mManager.getMessenger().getReceivedCount()));
//        System.out.println(String.format("\tError %d messages", mManager.getMessenger().getErrorsCount()));
        //        System.out.println(String.format("\tAverage time for connection %d ms",diff/count));
        mManager.close();
    }
}
