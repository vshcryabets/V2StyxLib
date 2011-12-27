package com.v2soft.styxlib.tests;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.zip.CRC32;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.StyxFileOutputStream;
import com.v2soft.styxlib.library.messages.base.enums.FileMode;

public class BigFiles {
    private StyxClientManager mManager;
    
    public BigFiles(String server, int port) throws UnknownHostException {
        mManager = new  StyxClientManager(InetAddress.getByName(server),
                port, false);
    }
    
    public void start(long size) throws IOException, StyxException, InterruptedException, TimeoutException {
        mManager.connect();
        int filecount = 10;
        byte [] buffer = new byte[256];
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
            StyxFileOutputStream out = file.create(FileMode.PERMISSION_BITMASK);
            
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
        long diff = System.currentTimeMillis()-startTime;
        System.out.println(String.format("Done in %d ms", diff/1000));
        System.out.println(String.format("\tTransmited %d messages", mManager.getMessenger().getTransmitedCount()));
        System.out.println(String.format("\tReceived %d messages", mManager.getMessenger().getReceivedCount()));
        System.out.println(String.format("\tError %d messages", mManager.getMessenger().getErrorsCount()));
//        System.out.println(String.format("\tAverage time for connection %d ms",diff/count));
    }
}
