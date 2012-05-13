package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.v2soft.styxlib.library.StyxClientManager;
import com.v2soft.styxlib.library.core.StyxByteBuffer;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRClunkMessage;
import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
import com.v2soft.styxlib.library.messages.StyxRFlushMessage;
import com.v2soft.styxlib.library.messages.StyxROpenMessage;
import com.v2soft.styxlib.library.messages.StyxRReadMessage;
import com.v2soft.styxlib.library.messages.StyxRStatMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxRWalkMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTClunkMessage;
import com.v2soft.styxlib.library.messages.StyxTOpenMessage;
import com.v2soft.styxlib.library.messages.StyxTReadMessage;
import com.v2soft.styxlib.library.messages.StyxTStatMessage;
import com.v2soft.styxlib.library.messages.StyxTWalkMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

/**
 * Client state handler (this class exists one per client)
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientState 
implements Closeable {
    private String mUserName;
    private DualStateBuffer mBuffer;
    private int mIOUnit;
    private SocketChannel mChannel;
    private StyxByteBuffer mOutputBuffer;
    private IVirtualStyxDirectory mServerRoot;
    private IVirtualStyxDirectory mClientRoot;
    private HashMap<Long, IVirtualStyxFile> mAssignedFiles;

    public ClientState(int iounit, 
            SocketChannel channel, 
            IVirtualStyxDirectory root) throws FileNotFoundException {
        mIOUnit = iounit;
        mBuffer = new DualStateBuffer(iounit*2);
        mOutputBuffer = new StyxByteBuffer(ByteBuffer.allocateDirect(iounit));
        mChannel = channel;
        mServerRoot = root;
        mAssignedFiles = new HashMap<Long, IVirtualStyxFile>();
        mUserName = "";
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
        try {
            switch (msg.getType()) {
            case Tversion:
                answer = new StyxRVersionMessage(mIOUnit, StyxClientManager.PROTOCOL);
                break;
            case Tattach:
                answer = processAttach((StyxTAttachMessage)msg);
                break;
            case Tstat:
                fid = ((StyxTStatMessage)msg).getFID();
                file = mAssignedFiles.get(fid);
                if ( file != null ) {
                    answer = new StyxRStatMessage(msg.getTag(), file.getStat());
                } else {
                    answer = getNoFIDError(msg, fid);
                }
                break;
            case Tclunk:
                fid = ((StyxTClunkMessage)msg).getFID();
                file = mAssignedFiles.remove(fid);
                if ( file == null ) {
                    answer = getNoFIDError(msg, fid);
                } else {
                    file.close(this);
                    answer = new StyxRClunkMessage(msg.getTag());
                }
                break;
            case Tflush:
                // TODO do something there
                answer = new StyxRFlushMessage(msg.getTag());
                break;
            case Twalk:
                answer = processTWalk((StyxTWalkMessage) msg);
                break;
            case Topen:
                answer = processTopen((StyxTOpenMessage)msg);
                break;
            case Tread:
                answer = processTread((StyxTReadMessage)msg);
                break;
            default:
                System.out.println("Got message:");
                System.out.println(msg.toString());
                break;
            }
        } catch (StyxErrorMessageException e) {
            answer = e.getErrorMessage();
            answer.setTag(msg.getTag());
        }
        if ( answer != null ) {
            sendMessage(answer);
        }
    }

    private StyxRAttachMessage processAttach(StyxTAttachMessage msg) {
        String mountPoint = msg.getMountPoint();
        mClientRoot = mServerRoot.getDirectory(mountPoint);
        mUserName = msg.getUserName(); 
        StyxRAttachMessage answer = new StyxRAttachMessage(msg.getTag(), mClientRoot.getQID());
        registerOpenedFile(((StyxTAttachMessage)msg).getFID(), mClientRoot );
        return answer;
    }

    /**
     * Handle read operation
     * @param msg
     * @return
     * @throws StyxErrorMessageException 
     */
    private StyxMessage processTread(StyxTReadMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        IVirtualStyxFile file = mAssignedFiles.get(fid);
        if ( file == null ) {
            return getNoFIDError(msg, fid);
        }
        byte [] buffer = new byte[mIOUnit]; 
        long readed = file.read(this, buffer, msg.getOffset(), msg.getCount());
        if ( buffer == null ) {
            StyxErrorMessageException.doException("Unable to read this file");
        }
        return new StyxRReadMessage(msg.getTag(), buffer, (int) readed);
    }

    /**
     * Handle TOpen message from client
     * @param msg
     * @return
     * @throws StyxErrorMessageException 
     * @throws IOException 
     */
    private StyxMessage processTopen(StyxTOpenMessage msg) throws StyxErrorMessageException, IOException {
        long fid = msg.getFID();
        IVirtualStyxFile file = mAssignedFiles.get(fid);
        if ( file == null ) {
            return getNoFIDError(msg, fid);
        }
        if ( file.open(this, msg.getMode()) ) {
            return new StyxROpenMessage(msg.getTag(), file.getQID(), mIOUnit-24 ); // TODO magic number
        } else {
            StyxErrorMessageException.doException("Incorrect mode for specified file");
            return null;
        }
    }

    private StyxMessage processTWalk(StyxTWalkMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        IVirtualStyxFile file = mAssignedFiles.get(fid);
        if ( file == null ) {
            return getNoFIDError(msg, fid);
        }

        if ( file instanceof IVirtualStyxDirectory ) {
            List<StyxQID> QIDList = new LinkedList<StyxQID>();
            IVirtualStyxFile walkFile = ((IVirtualStyxDirectory)file).walk(
                    msg.getPath(), 
                    QIDList);
            if ( walkFile != null ) {
                mAssignedFiles.put(msg.getNewFID(), walkFile);
                return new StyxRWalkMessage(msg.getTag(), QIDList);
            } else {
                return new StyxRErrorMessage(msg.getTag(), "file does not exist");
            }
        }
        return new StyxRErrorMessage(msg.getTag(), "file does not exist");
    }

    /**
     * 
     * @param tag message tag
     * @param fid File ID
     * @return new Rerror message
     * @throws StyxErrorMessageException 
     */
    private StyxRErrorMessage getNoFIDError(StyxMessage message, long fid) throws StyxErrorMessageException {
        StyxErrorMessageException.doException(String.format("Unknown FID (%d)", fid));
        return null;
    }

    private void registerOpenedFile(long fid, IVirtualStyxFile file) {
        mAssignedFiles.put(fid, file);
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
