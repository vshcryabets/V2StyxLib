package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Income Styx messages processor.
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class TMessagesProcessor extends QueueMessagesProcessor implements IMessageProcessor {
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
        mRoot.onConnectionOpened(clientDetails);
    }
    @Override
    public void removeClient(ClientDetails clientDetails) {
        mRoot.onConnectionClosed(clientDetails);
    }

    /**
     * Processing incoming messages
     * @param message incoming message
     */
    @Override
    public void processPacket(StyxMessage message, ClientDetails target) throws StyxException {
        mHandledPackets++;
        StyxMessage answer = null;
        IVirtualStyxFile file;
        long fid;
        try {
            switch (message.getType()) {
                case Tversion:
                    answer = new StyxRVersionMessage(mConnectionDetails.ioUnit(), mConnectionDetails.protocol());
                    break;
                case Tattach:
                    answer = processAttach(target, (StyxTAttachMessage)message);
                    break;
                case Tauth:
                    answer = processAuth(target, (StyxTAuthMessage)message);
                    break;
                case Tstat:
                    fid = ((StyxTMessageFID)message).getFID();
                    file = target.getAssignedFile(fid);
                    answer = new StyxRStatMessage(message.getTag(), file.getStat());
                    break;
                case Tclunk:
                    answer = processClunk(target, (StyxTMessageFID)message);
                    break;
                case Tflush:
                    // TODO do something there
                    answer = new StyxMessage(MessageType.Rflush, message.getTag());
                    break;
                case Twalk:
                    answer = processWalk(target, (StyxTWalkMessage) message);
                    break;
                case Topen:
                    answer = processOpen(target, (StyxTOpenMessage)message);
                    break;
                case Tread:
                    answer = processRead(target, (StyxTReadMessage)message);
                    break;
                case Twrite:
                    answer = processWrite(target, (StyxTWriteMessage)message);
                    break;
                case Twstat:
                    answer = processWStat((StyxTWStatMessage)message);
                    break;
                case Tcreate:
                    answer = processCreate(target, (StyxTCreateMessage)message);
                    break;
                case Tremove:
                    answer = processRemove(target, (StyxTMessageFID)message);
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
            target.getDriver().sendMessage(answer, target);
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
        Credentials credentials = new CredentialsImpl(msg.getUserName(), null);
        clientDetails.setCredentials(credentials);
        String mountPoint = msg.getMountPoint();
        IVirtualStyxFile root = mRoot; // TODO .getDirectory(mountPoint); there should be some logic with mountPoint?
        StyxRAttachMessage answer = new StyxRAttachMessage(msg.getTag(), root.getQID());
        clientDetails.registerOpenedFile(msg.getFID(), root );
        return answer;
    }

    private StyxMessage processAuth(ClientDetails clientDetails, StyxTAuthMessage msg) {
        Credentials credentials = new CredentialsImpl(msg.getUserName(), null);
        clientDetails.setCredentials(credentials);
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
    private StyxMessage processOpen(ClientDetails clientDetails, StyxTOpenMessage msg)
            throws StyxException {
        long fid = msg.getFID();
        IVirtualStyxFile file = clientDetails.getAssignedFile(fid);
        if ( file.open(clientDetails, msg.getMode()) ) {
            return new StyxROpenMessage(msg.getTag(), file.getQID(),
                    mConnectionDetails.ioUnit() - DEFAULT_PACKET_HEADER_SIZE, false);
        } else {
            throw StyxErrorMessageException.newInstance("Not supported mode for specified file");
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
        return new StyxROpenMessage(msg.getTag(), qid, mConnectionDetails.ioUnit(), true);
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
        if (msg.getCount() > mConnectionDetails.ioUnit()) {
            return new StyxRErrorMessage(msg.getTag(), "IOUnit overflow");
        }
        long fid = msg.getFID();
        byte [] buffer = new byte[(int) msg.getCount()];
        MetricsAndStats.byteArrayAllocationRRead++;
        return new StyxRReadMessage(msg.getTag(), buffer,
                (int) clientDetails.getAssignedFile(fid).read(clientDetails, buffer, msg.getOffset(), msg.getCount()));
    }

    public IVirtualStyxFile getRoot() {
        return mRoot;
    }

    public void setRoot(IVirtualStyxFile root) {
        mRoot = root;
    }
}
