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
import com.v2soft.styxlib.library.messages.StyxRClunkMessage;
import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
import com.v2soft.styxlib.library.messages.StyxRStatMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxRWalkMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTClunkMessage;
import com.v2soft.styxlib.library.messages.StyxTStatMessage;
import com.v2soft.styxlib.library.messages.StyxTWalkMessage;
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
		mBuffer = new DualStateBuffer(iounit*2);
		mOutputBuffer = new StyxByteBuffer(ByteBuffer.allocateDirect(iounit));
		mChannel = channel;
		mServerRoot = root;
		mOpenedFiles = new HashMap<Long, IVirtualStyxFile>();
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
		IVirtualStyxFile file;
		long fid;
		switch (msg.getType()) {
		case Tversion:
			answer = new StyxRVersionMessage(mIOUnit, StyxClientManager.PROTOCOL);
			break;
		case Tattach:
			String mountPoint = ((StyxTAttachMessage)msg).getMountPoint();
			mClientRoot = mServerRoot.getDirectory(mountPoint);
			answer = new StyxRAttachMessage(msg.getTag(), mClientRoot.getQID());
			registerOpenedFile(((StyxTAttachMessage)msg).getFID(), mClientRoot );
			break;
		case Tstat:
		    fid = ((StyxTStatMessage)msg).getFID();
			file = mOpenedFiles.get(fid);
			if ( file != null ) {
				answer = new StyxRStatMessage(msg.getTag(), file.getStat());
			} else {
			    answer = getNoFIDError(msg, fid);
			}
			break;
		case Tclunk:
		    fid = ((StyxTClunkMessage)msg).getFID();
			file = mOpenedFiles.remove(fid);
			if ( file == null ) {
			    answer = getNoFIDError(msg, fid);
			} else {
				answer = new StyxRClunkMessage(msg.getTag());
			}
			break;
		case Twalk:
		    fid = ((StyxTWalkMessage)msg).getFID();
            file = mOpenedFiles.get(fid);
            if ( file == null ) {
                answer = getNoFIDError(msg, fid);
            } else {
                if ( file instanceof IVirtualStyxDirectory ) {
                    IVirtualStyxFile newFile = ((IVirtualStyxDirectory)file).walk(((StyxTWalkMessage)msg).getPath());
//                    answer = new StyxRWalkMessage(msg.getTag()).se
                    
                }
                
            }
		default:
			break;
		}
		if ( answer != null ) {
			sendMessage(answer);
		}
	}

	/**
	 * 
	 * @param tag message tag
	 * @param fid File ID
	 * @return new Rerror message
	 */
	private StyxRErrorMessage getNoFIDError(StyxMessage message, long fid) {
	    return new StyxRErrorMessage(message.getTag(), 
	            String.format("Unknown FID (%d)", fid));
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
