package com.v2soft.styxlib.handlers;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxNotAuthorizedException;
import com.v2soft.styxlib.exceptions.StyxUnknownClientIdException;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxTAttachMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.server.ClientsRepo;
import com.v2soft.styxlib.l5.dev.MetricsAndStats;

import java.util.LinkedList;
import java.util.List;

/**
 * Income Styx messages processor.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class TMessagesProcessor extends QueueMessagesProcessor {
    protected ConnectionDetails mConnectionDetails;
    private static final int DEFAULT_PACKET_HEADER_SIZE = 24;
    private IVirtualStyxFile mRoot;
    protected int mHandledPackets, mErrorPackets, mAnswerPackets;
    private ClientsRepo mClientsRepo;

    public TMessagesProcessor(ConnectionDetails details,
                              IVirtualStyxFile root,
                              ClientsRepo clientsRepo) {
        mConnectionDetails = details;
        mRoot = root;
        mHandledPackets = 0;
        mErrorPackets = 0;
        mAnswerPackets = 0;
        mClientsRepo = clientsRepo;
    }

    @Override
    public void onClientRemoved(int clientId) {
        mRoot.close(clientId);
    }

    /**
     * Processing incoming messages
     *
     * @param message incoming message
     */
    @Override
    public void processPacket(StyxMessage message, int clientId) throws StyxException {
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
                    answer = processAttach(clientId, (StyxTAttachMessage)message);
                    break;
                case MessageType.Tauth:
                    answer = processAuth(clientId, (StyxTAuthMessage)message);
                    break;
                case MessageType.Tstat:
                    fid = ((StyxTMessageFID)message).getFID();
                    file = mClientsRepo.getAssignedFile(clientId, fid);
                    answer = new StyxRStatMessage(message.getTag(), file.getStat());
                    break;
                case MessageType.Tclunk:
                    answer = processClunk(clientId, (StyxTMessageFID)message);
                    break;
                case MessageType.Tflush:
                    // TODO do something there
                    answer = new BaseMessage(MessageType.Rflush, message.getTag(), null);
                    break;
                case MessageType.Twalk:
                    answer = processWalk(clientId, (StyxTWalkMessage) message);
                    break;
                case MessageType.Topen:
                    answer = processOpen(clientId, (StyxTOpenMessage)message);
                    break;
                case MessageType.Tread:
                    answer = processRead(clientId, (StyxTReadMessage)message);
                    break;
                case MessageType.Twrite:
                    answer = processWrite(clientId, (StyxTWriteMessage)message);
                    break;
                case MessageType.Twstat:
                    answer = processWStat((StyxTWStatMessage)message);
                    break;
                case MessageType.Tcreate:
                    answer = processCreate(clientId, (StyxTCreateMessage)message);
                    break;
                case MessageType.Tremove:
                    answer = processRemove(clientId, (StyxTMessageFID)message);
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
        } catch (StyxNotAuthorizedException e) {
            answer = new StyxRErrorMessage(message.getTag(), "Not authorized");
            mErrorPackets++;
        } catch (StyxUnknownClientIdException e) {
            answer = new StyxRErrorMessage(message.getTag(), "Unknown client ID, try to reconnect");
            mErrorPackets++;
        } catch (StyxException e) {
            answer = new StyxRErrorMessage(message.getTag(), e.getMessage());
            mErrorPackets++;
        }
        if (answer != null) {
            mAnswerPackets++;
            mClientsRepo.getDriver(clientId).sendMessage(answer, clientId, 0);
        }
    }

    private StyxRAttachMessage processAttach(int clientId, StyxTAttachMessage msg) throws StyxUnknownClientIdException {
        var clientDetails = mClientsRepo.getClient(clientId);
        clientDetails.setUsername(msg.userName);
        String mountPoint = msg.mountPoint;
        mRoot.onConnectionOpened(clientId);
        IVirtualStyxFile root = mRoot; // TODO .getDirectory(mountPoint); there should be some logic with mountPoint?
        StyxRAttachMessage answer = new StyxRAttachMessage(msg.getTag(), root.getQID());
        clientDetails.registerOpenedFile(msg.getFID(), root);
        return answer;
    }

    private StyxMessage processAuth(int clientId, StyxTAuthMessage msg) throws StyxUnknownClientIdException {
        mClientsRepo.getClient(clientId).setUsername(msg.mUserName);
        // TODO handle auth packet
        return new StyxRAuthMessage(msg.getTag(), StyxQID.EMPTY);
    }

    private StyxMessage processClunk(int clientId, StyxTMessageFID msg)
            throws StyxException {
        mClientsRepo.closeFile(clientId, msg.getFID());
        return new BaseMessage(MessageType.Rclunk, msg.getTag(), null);
    }

    private StyxMessage processWalk(int clientId, StyxTWalkMessage msg) throws StyxException {
        long fid = msg.getFID();
        final List<StyxQID> qidsList = new LinkedList<StyxQID>();
        final IVirtualStyxFile walkFile = mClientsRepo.getAssignedFile(clientId, fid).walk(
                clientId,
                new LinkedList<>(msg.getPathElements()),
                qidsList);
        if (walkFile != null) {
            mClientsRepo.getClient(clientId).registerOpenedFile(msg.getNewFID(), walkFile);
            return new StyxRWalkMessage(msg.getTag(), qidsList);
        } else {
            return new StyxRErrorMessage(msg.getTag(),
                    String.format("file \"%s\" does not exist", msg.getPathElements()));
        }
    }

    private StyxMessage processOpen(int clientId, StyxTOpenMessage msg)
            throws StyxException {
        long fid = msg.getFID();
        IVirtualStyxFile file = mClientsRepo.getAssignedFile(clientId, fid);
        if (file.open(clientId, msg.mode)) {
            return new StyxROpenMessage(msg.getTag(), file.getQID(),
                    mConnectionDetails.ioUnit() - DEFAULT_PACKET_HEADER_SIZE, false);
        } else {
            throw StyxErrorMessageException.newInstance("Not supported mode for specified file");
        }
    }

    private StyxMessage processRemove(int clientId, StyxTMessageFID msg)
            throws StyxException {
        if (mClientsRepo.getAssignedFile(clientId, msg.getFID()).delete(clientId)) {
            return new BaseMessage(MessageType.Rremove, msg.getTag(), null);
        } else {
            return new StyxRErrorMessage(msg.getTag(), "Can't delete file");
        }
    }

    private StyxMessage processCreate(int clientId, StyxTCreateMessage msg)
            throws StyxException {
        final IVirtualStyxFile file = mClientsRepo.getAssignedFile(clientId, msg.getFID());
        StyxQID qid = file.create(clientId, msg.name, msg.permissions, msg.mode);
        return new StyxROpenMessage(msg.getTag(), qid, mConnectionDetails.ioUnit(), true);
    }

    private StyxMessage processWStat(StyxTWStatMessage msg) {
        // TODO handle Twstat
        return new BaseMessage(MessageType.Rwstat, msg.getTag(), null);
    }

    private StyxMessage processWrite(int clientId, StyxTWriteMessage msg) throws StyxException {
        long fid = msg.getFID();
        return new StyxRWriteMessage(msg.getTag(),
                mClientsRepo.getAssignedFile(clientId, fid).write(clientId, msg.data, msg.offset));
    }

    private StyxMessage processRead(int clientId, StyxTReadMessage msg) throws StyxException {
        if (msg.count > mConnectionDetails.ioUnit()) {
            return new StyxRErrorMessage(msg.getTag(), "IOUnit overflow");
        }
        long fid = msg.getFID();
        byte[] buffer = new byte[(int) msg.count];
        MetricsAndStats.byteArrayAllocationRRead++;
        return new StyxRReadMessage(msg.getTag(), buffer,
                mClientsRepo
                        .getAssignedFile(clientId, fid)
                        .read(clientId, buffer, msg.offset, msg.count));
    }

    public IVirtualStyxFile getRoot() {
        return mRoot;
    }

    public void setRoot(IVirtualStyxFile root) {
        mRoot = root;
    }
}
