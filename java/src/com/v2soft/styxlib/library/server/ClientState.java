package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.core.StyxByteBuffer;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
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
	private SocketChannel mChannel;
	private StyxByteBuffer mOutputBuffer;

	public ClientState(int iounit, SocketChannel channel) throws FileNotFoundException {
		mIOUnit = iounit;
		mBuffer =new DualStateBuffer(iounit*2);
		mOutputBuffer = new StyxByteBuffer(ByteBuffer.allocateDirect(iounit));
		mCount = 0;
		mChannel = channel;
	}

	private boolean process() throws IOException {
	    int inBuffer = mBuffer.remainsToRead();
	    if ( inBuffer > 4 ) {
	        long packetSize = mBuffer.getUInt32();
	        if ( inBuffer >= packetSize ) {
	            StyxMessage msg = StyxMessage.factory(mBuffer, mIOUnit);
	            if ( msg instanceof StyxTVersionMessage ) {
	            	// answer
	            	StyxRVersionMessage answer = new StyxRVersionMessage(mIOUnit, StyxClientManager.PROTOCOL);
	            	sendMessage(answer);
	            }
	            System.out.print("Got message "+msg.toString());
	            return true;
	        }
	    }
	    return false;
	}

	private void sendMessage(StyxMessage answer) throws IOException {
		answer.writeToBuffer(mOutputBuffer);
		mOutputBuffer.getBuffer().position(0);
		mChannel.write(mOutputBuffer.getBuffer());
	}

	@Override
	public void close() throws IOException {
		// write the rest data in buffer
//		int rest = mBuffer.remainsToRead();
//		if ( rest > 0 ) {
//			byte [] out = new byte[rest];
//			mBuffer.read(out, 0, rest);
//			mTestOut.write(out, 0, rest);
//			mTestOut.close();
//			mCount += rest;
//			System.out.print("\rReaded "+mCount);
//
//		}
		mBuffer = null;
		mCount = 0;
	}

    public boolean read() throws IOException {
        int readed = mBuffer.readFromChannel(mChannel);
        if ( readed == -1 ) {
            close();
            return true;
        } else {
        	while ( process() );
        }
        return false;
    }
}
