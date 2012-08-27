package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.io.StyxByteBufferWriteable;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRAuthMessage;
import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
import com.v2soft.styxlib.library.messages.StyxROpenMessage;
import com.v2soft.styxlib.library.messages.StyxRReadMessage;
import com.v2soft.styxlib.library.messages.StyxRStatMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxRWalkMessage;
import com.v2soft.styxlib.library.messages.StyxRWriteMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTAuthMessage;
import com.v2soft.styxlib.library.messages.StyxTCreateMessage;
import com.v2soft.styxlib.library.messages.StyxTOpenMessage;
import com.v2soft.styxlib.library.messages.StyxTReadMessage;
import com.v2soft.styxlib.library.messages.StyxTWStatMessage;
import com.v2soft.styxlib.library.messages.StyxTWalkMessage;
import com.v2soft.styxlib.library.messages.StyxTWriteMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

/**
 * Client state handler (this class exists one per client)
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientState 
implements Closeable {
    private String mUserName;
    private String mProtocol;
    private StyxByteBufferReadable mBuffer;
    private int mIOUnit;
    private SocketChannel mChannel;
    private StyxByteBufferWriteable mOutputBuffer;
    private IVirtualStyxFile mServerRoot;
    private IVirtualStyxFile mClientRoot;
    private HashMap<Long, IVirtualStyxFile> mAssignedFiles;

    public ClientState(int iounit, 
            SocketChannel channel, 
            IVirtualStyxFile root,
            String protocol) throws FileNotFoundException {
        if ( channel == null ) throw new NullPointerException("Client channel is null");
        if ( root == null ) throw new NullPointerException("Root is null");
        if ( protocol == null ) throw new NullPointerException("Protocol is null");
        mIOUnit = iounit;
        mBuffer = new StyxByteBufferReadable(iounit*2);
        mOutputBuffer = new StyxByteBufferWriteable(iounit);
        mChannel = channel;
        mServerRoot = root;
        mAssignedFiles = new HashMap<Long, IVirtualStyxFile>();
        mUserName = "nobody";
        mProtocol = protocol;
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
     * @param msg incomming message
     * @throws IOException 
     */
    private void processMessage(StyxMessage msg) throws IOException {
        //        System.out.print("Got message "+msg.toString());
        StyxMessage answer = null;
        IVirtualStyxFile file;
        long fid;
        try {
            switch (msg.getType()) {
            case Tversion:
                answer = new StyxRVersionMessage(mIOUnit, mProtocol);
                break;
            case Tattach:
                answer = processAttach((StyxTAttachMessage)msg);
                break;
            case Tauth:
                answer = processAuth((StyxTAuthMessage)msg);
                break;
            case Tstat:
                fid = ((StyxTMessageFID)msg).getFID();
                file = getAssignedFile(fid);
                answer = new StyxRStatMessage(msg.getTag(), file.getStat());
                break;
            case Tclunk:
                answer = processClunk((StyxTMessageFID)msg);
                break;
            case Tflush:
                // TODO do something there
                answer = new StyxMessage(MessageType.Rflush, msg.getTag());
                break;
            case Twalk:
                answer = processWalk((StyxTWalkMessage) msg);
                break;
            case Topen:
                answer = processOpen((StyxTOpenMessage)msg);
                break;
            case Tread:
                answer = processRead((StyxTReadMessage)msg);
                break;
            case Twrite:
                answer = processWrite((StyxTWriteMessage)msg);
                break;
            case Twstat:
                answer = processWStat((StyxTWStatMessage)msg);
                break;
            case Tcreate:
                answer = processCreate((StyxTCreateMessage)msg);
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

    /**
     * Handle Tcreate message
     * @param msg
     * @return
     * @throws StyxErrorMessageException 
     */
    private StyxMessage processCreate(StyxTCreateMessage msg) 
            throws StyxErrorMessageException {
        final IVirtualStyxFile file = getAssignedFile(msg.getFID());
        StyxQID qid = file.create(msg.getName(), msg.getPermissions(), msg.getMode());
        return new StyxROpenMessage(msg.getTag(), qid, mIOUnit, true);
    }

    /**
     * Handle Tclunk message
     * @param msg
     * @return
     * @throws StyxErrorMessageException
     */
    private StyxMessage processClunk(StyxTMessageFID msg) throws StyxErrorMessageException {
        IVirtualStyxFile file = getAssignedFile(msg.getFID());
        file.close(this);
        mAssignedFiles.remove(msg.getFID());
        return new StyxMessage(MessageType.Rclunk, msg.getTag());
    }

    private IVirtualStyxFile getAssignedFile(long fid) throws StyxErrorMessageException {
        if ( !mAssignedFiles.containsKey(fid) ) {
            StyxErrorMessageException.doException(
                    String.format("Unknown FID (%d)", fid));
        }
        return mAssignedFiles.get(fid); 
    }

    private StyxMessage processWStat(StyxTWStatMessage msg) {
        // TODO Auto-generated method stub
        return new StyxMessage(MessageType.Rwstat, msg.getTag());
    }

    /**
     * Handle TWrite messages
     * @param msg
     * @return
     * @throws StyxErrorMessageException 
     */
    private StyxMessage processWrite(StyxTWriteMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        IVirtualStyxFile file = getAssignedFile(fid);
        int writed = file.write(this, msg.getData(), msg.getOffset());
        return new StyxRWriteMessage(msg.getTag(), (int) writed);        
    }

    private StyxMessage processAuth(StyxTAuthMessage msg) {
        mUserName = msg.getUserName();
        return new StyxRAuthMessage(msg.getTag(), StyxQID.EMPTY);
    }

    private StyxRAttachMessage processAttach(StyxTAttachMessage msg) {
        String mountPoint = msg.getMountPoint();
        mClientRoot = mServerRoot; // TODO .getDirectory(mountPoint); there should be some logic with mountPoint?
        mUserName = msg.getUserName(); 
        StyxRAttachMessage answer = new StyxRAttachMessage(msg.getTag(), mClientRoot.getQID());
        registerOpenedFile(msg.getFID(), mClientRoot );
        return answer;
    }

    /**
     * Handle read operation
     * @param msg
     * @return
     * @throws StyxErrorMessageException 
     */
    private StyxMessage processRead(StyxTReadMessage msg) throws StyxErrorMessageException {
        if ( msg.getCount() > mIOUnit ) {
            return new StyxRErrorMessage(msg.getTag(), "IOUnit overflow");
        }
        long fid = msg.getFID();        
        IVirtualStyxFile file = getAssignedFile(fid);
        byte [] buffer = new byte[(int) msg.getCount()]; 
        long readed = file.read(this, buffer, msg.getOffset(), msg.getCount());
        return new StyxRReadMessage(msg.getTag(), buffer, (int) readed);
    }

    /**
     * Handle TOpen message from client
     * @param msg
     * @return
     * @throws StyxErrorMessageException 
     * @throws IOException 
     */
    private StyxMessage processOpen(StyxTOpenMessage msg) throws StyxErrorMessageException, IOException {
        long fid = msg.getFID();
        IVirtualStyxFile file = getAssignedFile(fid);
        if ( file.open(this, msg.getMode()) ) {
            return new StyxROpenMessage(msg.getTag(), file.getQID(), mIOUnit-24, false ); // TODO magic number
        } else {
            StyxErrorMessageException.doException("Incorrect mode for specified file");
            return null;
        }
    }

    /**
     * Handle TWalk message from client
     * @param msg
     * @return
     * @throws StyxErrorMessageException 
     * @throws IOException 
     */
    private StyxMessage processWalk(StyxTWalkMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        IVirtualStyxFile file = getAssignedFile(fid);
        List<StyxQID> QIDList = new LinkedList<StyxQID>();
        final IVirtualStyxFile walkFile = file.walk(
                msg.getPathElements().iterator(), 
                QIDList);    
        if ( walkFile != null ) {
            mAssignedFiles.put(msg.getNewFID(), walkFile);
            return new StyxRWalkMessage(msg.getTag(), QIDList);
        } else {
            return new StyxRErrorMessage(msg.getTag(), 
                    String.format("file \"%s\" does not exist",msg.getPath()));
        }
    }

    /**
     * 
     * @param tag message tag
     * @param fid File ID
     * @return new Rerror message
     * @throws StyxErrorMessageException 
     */
    //    private StyxRErrorMessage getNoFIDError(StyxMessage message, long fid) throws StyxErrorMessageException {
    //        
    //        return null;
    //    }

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
    public boolean readSocket() throws IOException {
        int read = 0;
        try {
            read = mBuffer.readFromChannel(mChannel);
        }
        catch (IOException e) {
            read = -1;
        }
        if ( read == -1 ) {
            close();
            return true;
        } else {
            while ( process() );
        }
        return false;
    }
}
