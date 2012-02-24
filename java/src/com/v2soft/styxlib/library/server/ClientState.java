package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.v2soft.styxlib.library.messages.base.StyxMessage;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientState 
implements Closeable {
	private DualStateBuffer mBuffer;
	private int mIOUnit;
	private long mCount;
	private FileOutputStream mTestOut;

	public ClientState(int iounit) throws FileNotFoundException {
		mIOUnit = iounit;
		mBuffer =new DualStateBuffer(iounit*2);
		mCount = 0;
		mTestOut = new FileOutputStream("test.out");
	}

	public void process() throws IOException {
	    int inBuffer = mBuffer.remainsToRead();
	    if ( inBuffer > 4 ) {
	        long packetSize = mBuffer.getUInt32();
	        if ( inBuffer >= packetSize ) {
	            StyxMessage msg = StyxMessage.factory(mBuffer, mIOUnit);
	            System.out.print("Got message "+msg.toString());
	        }
	    }
	}

	@Override
	public void close() throws IOException {
		// write the rest data in buffer
		int rest = mBuffer.remainsToRead();
		if ( rest > 0 ) {
			byte [] out = new byte[rest];
			mBuffer.read(out, 0, rest);
			mTestOut.write(out, 0, rest);
			mTestOut.close();
			mCount += rest;
			System.out.print("\rReaded "+mCount);

		}
		mBuffer = null;
		mCount = 0;
	}

    public boolean read(SocketChannel channel) throws IOException {
        int readed = mBuffer.readFromChannel(channel);
        if ( readed == -1 ) {
            close();
            return true;
        } else {
            process();
        }
        return false;
    }
}
