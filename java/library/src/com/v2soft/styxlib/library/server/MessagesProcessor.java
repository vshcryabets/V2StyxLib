package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.io.StyxDataWriter;
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
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class MessagesProcessor implements Closeable {
    private String mProtocol;
    private int mIOUnit;
//    private Set<ClientState> mClients;
    private IVirtualStyxFile mRoot;

    public MessagesProcessor(int iounit, IVirtualStyxFile root, String protocol) throws IOException {
        mIOUnit = iounit;
//        mClients = new HashSet<ClientState>();
        mRoot = root;
        mProtocol = protocol;
    }

    public void addClient(ClientState client) {
        client.setIOUnit(mIOUnit);
        client.setRoot(mRoot);
        client.setProtocol(mProtocol);
//        mClients.add(client);
    }

    @Override
    public void close() throws IOException {
    }

    protected void removeClient(ClientState client) {
        mRoot.onConnectionClosed(client);
//        mClients.remove(client);
    }

    /**
     * Processing incoming messages
     * @param message incomming message
     * @throws IOException
     */
    public void processPacket(ClientState client, StyxMessage message) throws IOException {
        //        System.out.print("Got message "+msg.toString());
        StyxMessage answer = null;
        IVirtualStyxFile file;
        long fid;
        try {
            switch (message.getType()) {
                case Tversion:
                    answer = new StyxRVersionMessage(mIOUnit, mProtocol);
                    break;
                case Tattach:
                    answer = processAttach(client, (StyxTAttachMessage)message);
                    break;
                case Tauth:
                    answer = processAuth(client, (StyxTAuthMessage)message);
                    break;
                case Tstat:
                    fid = ((StyxTMessageFID)message).getFID();
                    file = client.getAssignedFile(fid);
                    answer = new StyxRStatMessage(message.getTag(), file.getStat());
                    break;
                case Tclunk:
                    answer = processClunk(client, (StyxTMessageFID)message);
                    break;
                case Tflush:
                    // TODO do something there
                    answer = new StyxMessage(MessageType.Rflush, message.getTag());
                    break;
                case Twalk:
                    answer = processWalk(client, (StyxTWalkMessage) message);
                    break;
                case Topen:
                    answer = processOpen(client, (StyxTOpenMessage)message);
                    break;
                case Tread:
                    answer = processRead(client, (StyxTReadMessage)message);
                    break;
                case Twrite:
                    answer = processWrite(client, (StyxTWriteMessage)message);
                    break;
                case Twstat:
                    answer = processWStat((StyxTWStatMessage)message);
                    break;
                case Tcreate:
                    answer = processCreate(client, (StyxTCreateMessage)message);
                    break;
                case Tremove:
                    answer = processRemove(client, (StyxTMessageFID)message);
                    break;
                default:
                    System.out.println("Got message:");
                    System.out.println(message.toString());
                    break;
            }
        } catch (StyxErrorMessageException e) {
            answer = e.getErrorMessage();
            answer.setTag(message.getTag());
        }
        if ( answer != null ) {
            client.getDriver().sendMessage(client, answer);
        }
    }

    private StyxRAttachMessage processAttach(ClientState client, StyxTAttachMessage msg) {
        String mountPoint = msg.getMountPoint();
        client.setClientRoot(client.getServerRoot()); // TODO .getDirectory(mountPoint); there should be some logic with mountPoint?
        client.setUserName(msg.getUserName());
        StyxRAttachMessage answer = new StyxRAttachMessage(msg.getTag(), client.getClientRoot().getQID());
        client.registerOpenedFile(msg.getFID(), client.getClientRoot() );
        return answer;
    }

    private StyxMessage processAuth(ClientState client, StyxTAuthMessage msg) {
        client.setUserName(msg.getUserName());
        return new StyxRAuthMessage(msg.getTag(), StyxQID.EMPTY);
    }

    /**
     * Handle Tclunk message
     * @param msg
     * @return
     * @throws StyxErrorMessageException
     */
    private StyxMessage processClunk(ClientState client, StyxTMessageFID msg) throws StyxErrorMessageException {
        IVirtualStyxFile file = client.getAssignedFile(msg.getFID());
        file.close(client);
        client.closeFile(msg.getFID());
        return new StyxMessage(MessageType.Rclunk, msg.getTag());
    }

    /**
     * Handle TWalk message from client
     * @param msg
     * @return
     * @throws StyxErrorMessageException
     * @throws IOException
     */
    private StyxMessage processWalk(ClientState client, StyxTWalkMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        IVirtualStyxFile file = client.getAssignedFile(fid);
        List<StyxQID> QIDList = new LinkedList<StyxQID>();
        final IVirtualStyxFile walkFile = file.walk(
                msg.getPathElements().iterator(),
                QIDList);
        if ( walkFile != null ) {
            client.registerOpenedFile(msg.getNewFID(), walkFile);
            return new StyxRWalkMessage(msg.getTag(), QIDList);
        } else {
            return new StyxRErrorMessage(msg.getTag(),
                    String.format("file \"%s\" does not exist",msg.getPath()));
        }
    }

    /**
     * Handle TOpen message from client
     * @param msg
     * @return
     * @throws StyxErrorMessageException
     * @throws IOException
     */
    private StyxMessage processOpen(ClientState client, StyxTOpenMessage msg) throws StyxErrorMessageException, IOException {
        long fid = msg.getFID();
        IVirtualStyxFile file = client.getAssignedFile(fid);
        if ( file.open(client, msg.getMode()) ) {
            return new StyxROpenMessage(msg.getTag(), file.getQID(), mIOUnit-24, false ); // TODO magic number
        } else {
            StyxErrorMessageException.doException("Not supported mode for specified file");
            return null;
        }
    }

    /**
     * Handle Tremove
     * @param msg
     * @return
     * @throws StyxErrorMessageException
     */
    private StyxMessage processRemove(ClientState client, StyxTMessageFID msg)
            throws StyxErrorMessageException {
        final IVirtualStyxFile file = client.getAssignedFile(msg.getFID());
        if ( file.delete(client) ) {
            return new StyxMessage(MessageType.Rremove, msg.getTag());
        } else {
            return new StyxRErrorMessage(msg.getTag(), "Can't delete file");
        }
    }

    /**
     * Handle Tcreate message
     * @param msg
     * @return
     * @throws StyxErrorMessageException
     */
    private StyxMessage processCreate(ClientState client, StyxTCreateMessage msg)
            throws StyxErrorMessageException {
        final IVirtualStyxFile file = client.getAssignedFile(msg.getFID());
        StyxQID qid = file.create(msg.getName(), msg.getPermissions(), msg.getMode());
        return new StyxROpenMessage(msg.getTag(), qid, mIOUnit, true);
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
    private StyxMessage processWrite(ClientState client, StyxTWriteMessage msg) throws StyxErrorMessageException {
        long fid = msg.getFID();
        IVirtualStyxFile file = client.getAssignedFile(fid);
        int writed = file.write(client, msg.getData(), msg.getOffset());
        return new StyxRWriteMessage(msg.getTag(), (int) writed);
    }

    /**
     * Handle read operation
     * @param msg
     * @return
     * @throws StyxErrorMessageException
     */
    private StyxMessage processRead(ClientState client, StyxTReadMessage msg) throws StyxErrorMessageException {
        if ( msg.getCount() > mIOUnit ) {
            return new StyxRErrorMessage(msg.getTag(), "IOUnit overflow");
        }
        long fid = msg.getFID();
        IVirtualStyxFile file = client.getAssignedFile(fid);
        byte [] buffer = new byte[(int) msg.getCount()];
        long readed = file.read(client, buffer, msg.getOffset(), msg.getCount());
        return new StyxRReadMessage(msg.getTag(), buffer, (int) readed);
    }
}
