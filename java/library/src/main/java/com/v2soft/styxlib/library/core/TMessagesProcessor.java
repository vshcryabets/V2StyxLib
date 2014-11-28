package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
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
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Income Styx messages processor.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class TMessagesProcessor implements IMessageProcessor {
    protected ConnectionDetails mConnectionDetails;
    private static final int DEFAULT_PACKET_HEADER_SIZE = 24;
    private IVirtualStyxFile mRoot;
    protected int mHandledPackets, mErrorPackets, mAnswerPackets;

    public TMessagesProcessor(ConnectionDetails details, IVirtualStyxFile root) {
        mConnectionDetails = details;
        mRoot = root;
        mHandledPackets = 0;
        mErrorPackets = 0;
        mAnswerPackets = 0;
    }

    @Override
    public void addClient(ClientDetails clientDetails) {
    }
    @Override
    public void close() {
    }
    @Override
    public void removeClient(ClientDetails clientDetails) {
        mRoot.onConnectionClosed(clientDetails);
    }

    /**
     * Processing incoming messages
     * @param message incoming message
     * @throws IOException
     */
    @Override
    public void processPacket(StyxMessage message, ClientDetails clientDetails) throws IOException {
        mHandledPackets++;
        StyxMessage answer = null;
        IVirtualStyxFile file;
        long fid;
        try {
            switch (message.getType()) {
                case Tversion:
                    answer = new StyxRVersionMessage(mConnectionDetails.getIOUnit(), mConnectionDetails.getProtocol());
                    break;
                case Tattach:
                    answer = processAttach(clientDetails, (StyxTAttachMessage)message);
                    break;
                case Tauth:
                    answer = processAuth(clientDetails, (StyxTAuthMessage)message);
                    break;
                case Tstat:
                    fid = ((StyxTMessageFID)message).getFID();
                    file = clientDetails.getAssignedFile(fid);
                    answer = new StyxRStatMessage(message.getTag(), file.getStat());
                    break;
                case Tclunk:
                    answer = processClunk(clientDetails, (StyxTMessageFID)message);
                    break;
                case Tflush:
                    // TODO do something there
                    answer = new StyxMessage(MessageType.Rflush, message.getTag());
                    break;
                case Twalk:
                    answer = processWalk(clientDetails, (StyxTWalkMessage) message);
                    break;
                case Topen:
                    answer = processOpen(clientDetails, (StyxTOpenMessage)message);
                    break;
                case Tread:
                    answer = processRead(clientDetails, (StyxTReadMessage)message);
                    break;
                case Twrite:
                    answer = processWrite(clientDetails, (StyxTWriteMessage)message);
                    break;
                case Twstat:
                    answer = processWStat((StyxTWStatMessage)message);
                    break;
                case Tcreate:
                    answer = processCreate(clientDetails, (StyxTCreateMessage)message);
                    break;
                case Tremove:
                    answer = processRemove(clientDetails, (StyxTMessageFID)message);
                    break;
                default:
                    System.out.println("Got message:");
                    System.out.println(message.toString());
                    break;
            }
        } catch (StyxErrorMessageException e) {
            answer = e.getErrorMessage();
            answer.setTag(message.getTag());
            mErrorPackets++;
        }
        if ( answer != null ) {
            mAnswerPackets++;
            clientDetails.getDriver().sendMessage(answer, clientDetails);
        }
    }

    @Override
    public int getReceivedPacketsCount() {
        return mHandledPackets;
    }

    @Override
    public int getReceivedErrorPacketsCount() {
        return mErrorPackets;
    }

    private StyxRAttachMessage processAttach(ClientDetails clientDetails, StyxTAttachMessage msg) {
        String mountPoint = msg.getMountPoint();
        IVirtualStyxFile root = mRoot; // TODO .getDirectory(mountPoint); there should be some logic with mountPoint?
        StyxRAttachMessage answer = new StyxRAttachMessage(msg.getTag(), root.getQID());
        clientDetails.registerOpenedFile(msg.getFID(), root );
        return answer;
    }

    private StyxMessage processAuth(ClientDetails clientDetails, StyxTAuthMessage msg) {
        // TODO handle auth packet
        return new StyxRAuthMessage(msg.getTag(), StyxQID.EMPTY);
    }

    /**
     * Handle Tclunk message
     * @throws StyxErrorMessageException
     */
    private StyxMessage processClunk(ClientDetails clientDetails, StyxTMessageFID msg) throws StyxErrorMessageException {
        clientDetails.getAssignedFile(msg.getFID()).close(clientDetails);
        clientDetails.closeFile(msg.getFID());
        return new StyxMessage(MessageType.Rclunk, msg.getTag());
    }

    /**
     * Handle TWalk message from client
     * @throws StyxErrorMessageException
     * @throws IOException
     */
    private StyxMessage processWalk(ClientDetails clientDetails, StyxTWalkMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        List<StyxQID> QIDList = new LinkedList<StyxQID>();
        final IVirtualStyxFile walkFile = clientDetails.getAssignedFile(fid).walk(
                msg.getPathElements().iterator(),
                QIDList);
        if ( walkFile != null ) {
            clientDetails.registerOpenedFile(msg.getNewFID(), walkFile);
            return new StyxRWalkMessage(msg.getTag(), QIDList);
        } else {
            return new StyxRErrorMessage(msg.getTag(),
                    String.format("file \"%s\" does not exist",msg.getPath()));
        }
    }

    /**
     * Handle TOpen message from client
     * @throws StyxErrorMessageException
     * @throws IOException
     */
    private StyxMessage processOpen(ClientDetails clientDetails, StyxTOpenMessage msg) throws StyxErrorMessageException, IOException {
        long fid = msg.getFID();
        IVirtualStyxFile file = clientDetails.getAssignedFile(fid);
        if ( file.open(clientDetails, msg.getMode()) ) {
            return new StyxROpenMessage(msg.getTag(), file.getQID(),
                    mConnectionDetails.getIOUnit() - DEFAULT_PACKET_HEADER_SIZE, false );
        } else {
            StyxErrorMessageException.doException("Not supported mode for specified file");
            return null;
        }
    }

    /**
     * Handle Tremove
     * @throws StyxErrorMessageException
     */
    private StyxMessage processRemove(ClientDetails clientDetails, StyxTMessageFID msg)
            throws StyxErrorMessageException {
        if (clientDetails.getAssignedFile(msg.getFID()).delete(clientDetails) ) {
            return new StyxMessage(MessageType.Rremove, msg.getTag());
        } else {
            return new StyxRErrorMessage(msg.getTag(), "Can't delete file");
        }
    }

    /**
     * Handle Tcreate message
     * @throws StyxErrorMessageException
     */
    private StyxMessage processCreate(ClientDetails clientDetails, StyxTCreateMessage msg)
            throws StyxErrorMessageException {
        final IVirtualStyxFile file = clientDetails.getAssignedFile(msg.getFID());
        StyxQID qid = file.create(msg.getName(), msg.getPermissions(), msg.getMode());
        return new StyxROpenMessage(msg.getTag(), qid, mConnectionDetails.getIOUnit(), true);
    }

    private StyxMessage processWStat(StyxTWStatMessage msg) {
        // TODO handle Twstat
        return new StyxMessage(MessageType.Rwstat, msg.getTag());
    }

    /**
     * Handle TWrite messages
     * @throws StyxErrorMessageException
     */
    private StyxMessage processWrite(ClientDetails clientDetails, StyxTWriteMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        return new StyxRWriteMessage(msg.getTag(),
                clientDetails.getAssignedFile(fid).write(clientDetails, msg.getData(), msg.getOffset()));
    }

    /**
     * Handle read operation
     * @throws StyxErrorMessageException
     */
    private StyxMessage processRead(ClientDetails clientDetails, StyxTReadMessage msg) throws StyxErrorMessageException {
        if ( msg.getCount() > mConnectionDetails.getIOUnit() ) {
            return new StyxRErrorMessage(msg.getTag(), "IOUnit overflow");
        }
        long fid = msg.getFID();
        byte [] buffer = new byte[(int) msg.getCount()];
        return new StyxRReadMessage(msg.getTag(), buffer,
                (int) clientDetails.getAssignedFile(fid).read(clientDetails, buffer, msg.getOffset(), msg.getCount()));
    }

    public IVirtualStyxFile getRoot() {
        return mRoot;
    }
}
