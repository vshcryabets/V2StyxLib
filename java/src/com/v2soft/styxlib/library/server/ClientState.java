package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.core.StyxByteBuffer;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
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
	private SocketChannel mChannel;
	private StyxByteBuffer mOutputBuffer;

	public ClientState(int iounit, SocketChannel channel) throws FileNotFoundException {
		mIOUnit = iounit;
		mBuffer =new DualStateBuffer(iounit*2);
		mOutputBuffer = new StyxByteBuffer(ByteBuffer.allocateDirect(iounit));
		mChannel = channel;
	}

	/**
	 * 
	 * @return true if message was processed
	 * @throws IOException
	 */
	private boolean process() throws IOException {
	    int inBuffer = mBuffer.remainsToRead();
	    if ( inBuffer > 4 ) {
	        long packetSize = mBuffer.getUInt32();
	        if ( inBuffer >= packetSize ) {
	            final StyxMessage message = StyxMessage.factory(mBuffer, mIOUnit);
	            processMessage(message);
	            return true;
	        }
	    }
	    return false;
	}

	/**
	 * Processing incoming messages
	 * @param msg
	 * @throws IOException 
	 */
	private void processMessage(StyxMessage msg) throws IOException {
        System.out.print("Got message "+msg.toString());
        StyxMessage answer = null;
		switch (msg.getType()) {
		case Tversion:
        	answer = new StyxRVersionMessage(mIOUnit, StyxClientManager.PROTOCOL);
			break;
		case Tattach:
			answer = new StyxRAttachMessage(msg.getTag(), qid);
			break;
		default:
			break;
		}
		if ( answer != null ) {
			sendMessage(answer);
		}
	}

	private void sendMessage(StyxMessage answer) throws IOException {
		answer.writeToBuffer(mOutputBuffer);
		mOutputBuffer.getBuffer().position(0);
		mChannel.write(mOutputBuffer.getBuffer());
	}

	@Override
	public void close() throws IOException {
		mBuffer = null;
	}

	/**
	 * Read data from assigned SocketChannel
	 * @return
	 * @throws IOException
	 */
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
