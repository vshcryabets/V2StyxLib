package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.core.StyxByteBuffer;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRStatMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTStatMessage;
import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

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
	private IVirtualStyxDirectory mServerRoot;
	private IVirtualStyxDirectory mClientRoot;
	private HashMap<Long, IVirtualStyxFile> mOpenedFiles;

	public ClientState(int iounit, 
			SocketChannel channel, 
			IVirtualStyxDirectory root) throws FileNotFoundException {
		mIOUnit = iounit;
		mBuffer =new DualStateBuffer(iounit*2);
		mOutputBuffer = new StyxByteBuffer(ByteBuffer.allocateDirect(iounit));
		mChannel = channel;
		mServerRoot = root;
	}

	/**
	 * Process incomming message
	 * @return
	 * @throws IOException
	 */
	private boolean process() throws IOException {
	    int inBuffer = mBuffer.remainsToRead();
	    if ( inBuffer > 4 ) {
	        long packetSize = mBuffer.getUInt32();
	        if ( inBuffer >= packetSize ) {
	            StyxMessage message = StyxMessage.factory(mBuffer, mIOUnit);
	            if ( message instanceof StyxTVersionMessage ) {
	            	// answer
	            	StyxRVersionMessage answer = new StyxRVersionMessage(mIOUnit, StyxClientManager.PROTOCOL);
	            	sendMessage(answer);
	            } else if ( message instanceof StyxTAttachMessage ) {
	            	String mountPoint = ((StyxTAttachMessage)message).getMountPoint();
	            	mClientRoot = mServerRoot.getDirectory(mountPoint);
	            	StyxRAttachMessage answer = new StyxRAttachMessage(message.getTag(), mClientRoot.getQID());
	            	sendMessage(answer);
	            	registerOpenedFile(((StyxTAttachMessage)message).getFID(), mClientRoot );
	            } else if ( message instanceof StyxTStatMessage ) {
	            	IVirtualStyxFile file = mOpenedFiles.get(((StyxTStatMessage)message).getFID());
	            	StyxRStatMessage answer = new StyxRStatMessage(message.getTag(), file.getStat());
	            }
	            System.out.print("Got message "+message.toString());
	            return true;
	        }
	    }
	    return false;
	}

	private void registerOpenedFile(long fid, IVirtualStyxFile file) {
		mOpenedFiles.put(fid, file);
	}

	/**
	 * Send answer message to client
	 * @param answer
	 * @throws IOException
	 */
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
