package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;

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
	    if ( inBuffer >= mIOUnit ) {
            byte [] out = new byte[mIOUnit];
            mBuffer.read(out, 0, out.length);
            mTestOut.write(out, 0, mIOUnit);
            mCount += mIOUnit;
            System.out.print("\rReaded "+mCount);
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
