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

import java.util.LinkedList;
import java.util.List;

/**
 * Income Styx messages processor.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
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
        mRoot.close(clientDetails);
    }

    /**
     * Processing incoming messages
     *
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
                case MessageType.Tversion:
                    answer = new StyxRVersionMessage(mConnectionDetails.ioUnit(), mConnectionDetails.protocol());
                    break;
                case MessageType.Tattach:
                    answer = processAttach(target, (StyxTAttachMessage)message);
                    break;
                case MessageType.Tauth:
                    answer = processAuth(target, (StyxTAuthMessage)message);
                    break;
                case MessageType.Tstat:
                    fid = ((StyxTMessageFID)message).getFID();
                    file = target.getAssignedFile(fid);
                    answer = new StyxRStatMessage(message.getTag(), file.getStat());
                    break;
                case MessageType.Tclunk:
                    answer = processClunk(target, (StyxTMessageFID)message);
                    break;
                case MessageType.Tflush:
                    // TODO do something there
                    answer = new StyxMessage(MessageType.Rflush, message.getTag());
                    break;
                case MessageType.Twalk:
                    answer = processWalk(target, (StyxTWalkMessage) message);
                    break;
                case MessageType.Topen:
                    answer = processOpen(target, (StyxTOpenMessage)message);
                    break;
                case MessageType.Tread:
                    answer = processRead(target, (StyxTReadMessage)message);
                    break;
                case MessageType.Twrite:
                    answer = processWrite(target, (StyxTWriteMessage)message);
                    break;
                case MessageType.Twstat:
                    answer = processWStat((StyxTWStatMessage)message);
                    break;
                case MessageType.Tcreate:
                    answer = processCreate(target, (StyxTCreateMessage)message);
                    break;
                case MessageType.Tremove:
                    answer = processRemove(target, (StyxTMessageFID)message);
                    break;
                default:
                    System.out.println("Got message:");
                    System.out.println(message);
                    break;
            }
        } catch (StyxErrorMessageException e) {
            answer = e.getErrorMessage();
            answer.setTag(message.getTag());
            mErrorPackets++;
        }
        if (answer != null) {
            mAnswerPackets++;
            target.getDriver().sendMessage(answer, target, 0);
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
        clientDetails.registerOpenedFile(msg.getFID(), root);
        return answer;
    }

    private StyxMessage processAuth(ClientDetails clientDetails, StyxTAuthMessage msg) {
        Credentials credentials = new CredentialsImpl(msg.getUserName(), null);
        clientDetails.setCredentials(credentials);
        // TODO handle auth packet
        return new StyxRAuthMessage(msg.getTag(), StyxQID.EMPTY);
    }

    private StyxMessage processClunk(ClientDetails clientDetails, StyxTMessageFID msg) throws StyxErrorMessageException {
        clientDetails.getAssignedFile(msg.getFID()).close(clientDetails);
        clientDetails.closeFile(msg.getFID());
        return new StyxMessage(MessageType.Rclunk, msg.getTag());
    }

    private StyxMessage processWalk(ClientDetails clientDetails, StyxTWalkMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        final List<StyxQID> qidsList = new LinkedList<StyxQID>();
        final IVirtualStyxFile walkFile = clientDetails.getAssignedFile(fid).walk(
                msg.getPathElements().iterator(),
                qidsList);
        if (walkFile != null) {
            clientDetails.registerOpenedFile(msg.getNewFID(), walkFile);
            return new StyxRWalkMessage(msg.getTag(), qidsList);
        } else {
            return new StyxRErrorMessage(msg.getTag(),
                    String.format("file \"%s\" does not exist", msg.getPathElements()));
        }
    }

    private StyxMessage processOpen(ClientDetails clientDetails, StyxTOpenMessage msg)
            throws StyxException {
        long fid = msg.getFID();
        IVirtualStyxFile file = clientDetails.getAssignedFile(fid);
        if (file.open(clientDetails, msg.getMode())) {
            return new StyxROpenMessage(msg.getTag(), file.getQID(),
                    mConnectionDetails.ioUnit() - DEFAULT_PACKET_HEADER_SIZE, false);
        } else {
            throw StyxErrorMessageException.newInstance("Not supported mode for specified file");
        }
    }

    private StyxMessage processRemove(ClientDetails clientDetails, StyxTMessageFID msg)
            throws StyxErrorMessageException {
        if (clientDetails.getAssignedFile(msg.getFID()).delete(clientDetails)) {
            return new StyxMessage(MessageType.Rremove, msg.getTag());
        } else {
            return new StyxRErrorMessage(msg.getTag(), "Can't delete file");
        }
    }

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

    private StyxMessage processWrite(ClientDetails clientDetails, StyxTWriteMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        return new StyxRWriteMessage(msg.getTag(),
                clientDetails.getAssignedFile(fid).write(clientDetails, msg.getData(), msg.getOffset()));
    }

    private StyxMessage processRead(ClientDetails clientDetails, StyxTReadMessage msg) throws StyxErrorMessageException {
        if (msg.getCount() > mConnectionDetails.ioUnit()) {
            return new StyxRErrorMessage(msg.getTag(), "IOUnit overflow");
        }
        long fid = msg.getFID();
        byte[] buffer = new byte[(int) msg.getCount()];
        MetricsAndStats.byteArrayAllocationRRead++;
        return new StyxRReadMessage(msg.getTag(), buffer,
                (int) clientDetails
                        .getAssignedFile(fid)
                        .read(clientDetails, buffer, msg.getOffset(), msg.getCount()));
    }

    public IVirtualStyxFile getRoot() {
        return mRoot;
    }

    public void setRoot(IVirtualStyxFile root) {
        mRoot = root;
    }
}
