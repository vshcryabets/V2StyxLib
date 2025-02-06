package com.v2soft.styxlib.l6;

import com.v2soft.styxlib.exceptions.StyxEOFException;
import com.v2soft.styxlib.l5.IClient;
import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.serialization.impl.StyxSerializerImpl;
import com.v2soft.styxlib.l6.io.DualStreams;
import com.v2soft.styxlib.io.StyxDataInputStream;
import com.v2soft.styxlib.l6.io.StyxFileBufferedInputStream;
import com.v2soft.styxlib.l6.io.StyxUnbufferedInputStream;
import com.v2soft.styxlib.l6.io.StyxUnbufferedOutputStream;
import com.v2soft.styxlib.l5.messages.StyxROpenMessage;
import com.v2soft.styxlib.l5.messages.StyxRStatMessage;
import com.v2soft.styxlib.l5.messages.StyxRWalkMessage;
import com.v2soft.styxlib.l5.messages.StyxTCreateMessage;
import com.v2soft.styxlib.l5.messages.StyxTOpenMessage;
import com.v2soft.styxlib.l5.messages.StyxTWStatMessage;
import com.v2soft.styxlib.l5.messages.StyxTWalkMessage;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.handlers.IMessageTransmitter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxFile {
    public interface StyxFilenameFilter {
        boolean accept(StyxFile parent, String name);
    }

    public static final String SEPARATOR = "/";

    private final IClient mClient;
    private long mFID = StyxMessage.NOFID;
    private long mParentFID;
    private final String mPath;
    private final IMessageTransmitter mMessenger;
    private long mTimeout;
    protected ClientDetails mRecipient;

    public StyxFile(IClient client, String path, long parentFid) throws StyxException {
        if (!client.isConnected())
            throw new StyxException("No connection");
        mClient = client;
        mMessenger = mClient.getMessenger();
        mRecipient = mClient.getRecepient();
        mTimeout = mClient.getTimeout();
        mPath = path;
        mParentFID = parentFid;
    }

    public String getPath() {
        return mPath;
    }

    /**
     * Retrieve FID for this file
     *
     * @return FID allocated for this file
     */
    public long getFID()
            throws StyxException {
        if (mFID == StyxMessage.NOFID) {
            mFID = sendWalkMessage(mParentFID, mPath);
        }
        return mFID;
    }

    private int open(int mode, long fid)
            throws StyxException {
        final StyxTOpenMessage tOpen = new StyxTOpenMessage(fid, mode);
        final var rOpen = (StyxROpenMessage) mMessenger.sendMessage(tOpen, mRecipient, mTimeout).getResult();
        return (int) rOpen.ioUnit;
    }

    public void close() throws StyxException {
        if (mFID == StyxMessage.NOFID) {
            return;
        }
        // send Tclunk
        final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFID);
        var feature = mMessenger.sendMessage(tClunk, mRecipient, mTimeout);
        feature.getResult();

        mFID = StyxMessage.NOFID;
    }

    public List<StyxStat> listStat()
            throws StyxException {
        if (!isDirectory())
            return Collections.emptyList();
        var tempFID = getCloneFID();
        var iounit = open(ModeType.OREAD, tempFID);
        var stats = new ArrayList<StyxStat>();
        try {
            var is = new StyxFileBufferedInputStream(mMessenger, tempFID, iounit, mRecipient);
            var sis = new StyxDataInputStream(is);
            while (true) {
                stats.add(mClient.getDeserializer().deserializeStat(sis));
            }
        } catch (StyxEOFException e) {
            // That's ok
        } finally {
            // TODO ???
//            mRecepient.getPolls().getFIDPoll().release(mFID);
        }
        // why close file FID we have a cloned FID?
        close();
        return stats;
    }

    public long getCloneFID() throws StyxException {
        return sendWalkMessage(getFID(), "");
    }

    public List<String> list(StyxFilenameFilter filter)
            throws StyxException {
        if (filter == null) {
            return listStat()
                    .stream()
                    .map(StyxStat::name)
                    .toList();
        } else {
            return listStat()
                    .stream()
                    .map(StyxStat::name)
                    .filter(name -> filter.accept(this, name))
                    .toList();
        }
    }

    /**
     * Open input stream to this file.
     *
     * @return input stream
     */
    public StyxFileBufferedInputStream openForRead()
            throws StyxException {
        checkConnection();
        long tempFID = getCloneFID();
        int iounit = open(ModeType.OREAD, tempFID);
        return new StyxFileBufferedInputStream(mMessenger, tempFID, iounit, mRecipient);
    }

    private void checkConnection() throws StyxException {
        if (!mClient.isConnected()) {
            throw new StyxException("Not connected to server");
        }
    }

    /**
     * Get unbuffered input stream to this file.
     *
     * @return unbuffered input stream
     */
    public StyxUnbufferedInputStream openForReadUnbuffered() throws StyxException {
        checkConnection();
        long tempFID = getCloneFID();
        int iounit = open(ModeType.OREAD, tempFID);
        return new StyxUnbufferedInputStream(tempFID, mMessenger, iounit, mRecipient);
    }

    /**
     * Open both streams - input and output.
     */
    public DualStreams openForReadAndWrite() throws StyxException {
        return new DualStreams(openForRead(), openForWrite());
    }

    public OutputStream openForWrite()
            throws StyxException {
        return new BufferedOutputStream(openForWriteUnbuffered());
    }

    public StyxUnbufferedOutputStream openForWriteUnbuffered()
            throws StyxException {
        checkConnection();
        long clonedFID = getCloneFID();
        return new StyxUnbufferedOutputStream(clonedFID,
                mMessenger,
                mRecipient,
                open(ModeType.OWRITE, clonedFID)
        );
    }

    public void create(long permissions) throws StyxException {
        checkConnection();
        // reserve FID
        long tempFID = sendWalkMessage(mParentFID, "");
        var tCreate = new StyxTCreateMessage(tempFID, mPath, permissions, ModeType.OWRITE);
        mMessenger
            .sendMessage(tCreate, mRecipient, mTimeout)
//                .exceptionally()
            .getResult();
        // TODO reuse FID
//        mFID = tempFID;
        // close temp FID
        var tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, tempFID);
        mMessenger
            .sendMessage(tClunk, mRecipient, mTimeout)
            .getResult();
//        mRecipient.getPolls().releaseFID(tCreate);
    }

    public boolean exists() throws StyxException {
        try {
            long fid = getFID();
            return (fid != StyxMessage.NOFID);
        } catch (StyxErrorMessageException e) {
            return false;
        }
    }

    /**
     * Delete file or empty folder
     */
    public void delete()
            throws StyxException {
        delete(false);
    }

    /**
     * Delete file or folder
     *
     * @param recurse Recursive delete
     */
    public void delete(boolean recurse)
            throws StyxException {
        if (recurse && this.isDirectory()) {
            for (var name : list(null)) {
                var file = new StyxFile(mClient, name, mFID);
                file.delete(true);
            }
        }
        long fid = getFID();
        mFID = StyxMessage.NOFID;
        var tRemove = new StyxTMessageFID(MessageType.Tremove, MessageType.Rremove, fid);
        mMessenger
            .sendMessage(tRemove, mRecipient, mTimeout)
            .getResult();
    }

    public void renameTo(String name)
            throws StyxException {
        StyxStat stat = getStat();
        var newStat = new StyxStat(stat.type(),
                stat.dev(),
                stat.QID(),
                stat.mode(),
                stat.accessTime(),
                stat.modificationTime(),
                stat.length(),
                name,
                stat.userName(),
                stat.groupName(),
                stat.modificationUser());
        StyxTWStatMessage tWStat = new StyxTWStatMessage(getFID(), newStat);
        mMessenger.sendMessage(tWStat, mRecipient, mTimeout).getResult();
    }

    public void mkdir(long permissions) throws InterruptedException, StyxException {
        permissions = FileMode.getPermissionsByMode(permissions) | FileMode.Directory.getMode();
        StyxTCreateMessage tCreate = new StyxTCreateMessage(mParentFID, getName(), permissions, ModeType.OREAD);
        mMessenger.sendMessage(tCreate, mRecipient, mTimeout).getResult();
    }

    public boolean checkFileMode(FileMode mode)
            throws StyxException {
        StyxStat stat = getStat();
        return mode.check(stat.mode());
    }

    public boolean isDirectory()
            throws StyxException {
        return checkFileMode(FileMode.Directory);
    }

    public boolean isAppendOnly()
            throws StyxException {
        return checkFileMode(FileMode.AppendOnly);
    }

    public boolean isExclusiveUse()
            throws StyxException {
        return checkFileMode(FileMode.ExclusiveUse);
    }

    public boolean isMountedChannel()
            throws StyxException {
        return checkFileMode(FileMode.MountedChannel);
    }

    public boolean isAuthenticationFile() throws StyxException {
        return checkFileMode(FileMode.AuthenticationFile);
    }

    public boolean isTemporaryFile() throws StyxException {
        return checkFileMode(FileMode.TemporaryFile);
    }

    public boolean isReadOwner() throws StyxException {
        return checkFileMode(FileMode.ReadOwnerPermission);
    }

    public boolean isWriteOwner() throws StyxException {
        return checkFileMode(FileMode.WriteOwnerPermission);
    }

    public boolean isExecuteOwner() throws StyxException {
        return checkFileMode(FileMode.ExecuteOwnerPermission);
    }

    public boolean isReadGroup() throws StyxException {
        return checkFileMode(FileMode.ReadGroupPermission);
    }

    public boolean isWriteGroup() throws StyxException {
        return checkFileMode(FileMode.WriteGroupPermission);
    }

    public boolean isExecuteGroup() throws StyxException {
        return checkFileMode(FileMode.ExecuteGroupPermission);
    }

    public boolean isReadOthers() throws StyxException {
        return checkFileMode(FileMode.ReadOthersPermission);
    }

    public boolean isWriteOthers() throws StyxException {
        return checkFileMode(FileMode.WriteOthersPermission);
    }

    public boolean isExecuteOthers() throws StyxException {
        return checkFileMode(FileMode.ExecuteOthersPermission);
    }

    public String getName() throws StyxException, InterruptedException {
        //StyxStat stat = getStat();
        //return stat.getName();
        StringBuilder builder = new StringBuilder(getPath());
        while (builder.toString().startsWith(SEPARATOR))
            builder.delete(0, 1);
        while (builder.toString().endsWith(SEPARATOR))
            builder.delete(builder.length() - 1, builder.length());

        int index = builder.toString().lastIndexOf(SEPARATOR);
        if (index < 0)
            return builder.toString();

        builder.delete(0, index);
        return builder.toString();
    }

    private long sendWalkMessage(long parentFID, String path)
            throws StyxException {
        long newFID = mRecipient.getPolls().getFIDPoll().getFreeItem();
        final StyxTWalkMessage tWalk = new StyxTWalkMessage(parentFID,
                newFID, StyxSerializerImpl.splitPath(path));
        final var feature = mMessenger.sendMessage(tWalk, mRecipient, mTimeout);
        final StyxMessage rWalk = feature.getResult();
        StyxErrorMessageException.doException(rWalk, mPath);
        if (((StyxRWalkMessage) rWalk).getQIDListLength() != tWalk.getPathLength())
            throw new StyxException("File not found " + mPath);
        return newFID;
    }

    /**
     * Return stat info of this file
     *
     * @return return stat structure of the current file.
     */
    public StyxStat getStat()
            throws StyxException {
        final var rMessage = mMessenger.<StyxRStatMessage>sendMessage(
                new StyxTMessageFID(MessageType.Tstat, MessageType.Rstat, getFID()),
                mRecipient,
                mTimeout).getResult();
        return rMessage.stat;
    }

    public IClient getIClient() {
        return mClient;
    }

    public long getTimeout() {
        return mTimeout;
    }

    public void setTimeout(long mTimeout) {
        this.mTimeout = mTimeout;
    }

    public StyxFile walk(String path) throws StyxException {
        return new StyxFile(
                mClient,
                path,
                getFID()
        );
    }
}
