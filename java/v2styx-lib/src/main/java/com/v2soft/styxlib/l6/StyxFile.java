package com.v2soft.styxlib.l6;

import com.v2soft.styxlib.exceptions.StyxEOFException;
import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.io.StyxDataInputStream;
import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.enums.FileMode;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxRErrorMessage;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.l5.v9p2000.StyxSerializerImpl;
import com.v2soft.styxlib.l6.io.StyxFileBufferedInputStream;
import com.v2soft.styxlib.l6.io.StyxUnbufferedInputStream;
import com.v2soft.styxlib.l6.io.StyxUnbufferedOutputStream;
import com.v2soft.styxlib.utils.StyxSessionDI;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxFile {
    public static final String SEPARATOR = "/";
    private long mFID = Constants.NOFID;
    private final long mParentFID;
    private final String mPath;
    private final IMessageTransmitter mTransmitter;
    private final int mTimeout;
    private final int mClientId;
    private final StyxSessionDI mDI;

    public StyxFile(String path,
                    long parentFid,
                    int clientId,
                    IMessageTransmitter transmitter,
                    int timeout,
                    StyxSessionDI di) throws StyxException {
        mDI = di;
        mTransmitter = transmitter;
        mClientId = clientId;
        mTimeout = timeout;
        mPath = path;
        mParentFID = parentFid;
    }

    /**
     * Retrieve FID for this file
     *
     * @return FID allocated for this file
     */
    public long getFID()
            throws StyxException {
        if (mFID == Constants.NOFID) {
            mFID = sendWalkMessage(mParentFID, mPath);
        }
        return mFID;
    }

    private int open(int mode, long fid)
            throws StyxException {
        final StyxTOpenMessage tOpen = new StyxTOpenMessage(fid, mode);
        final var rOpen = (StyxROpenMessage) mTransmitter.sendMessage(tOpen, mClientId, mTimeout).getResult();
        return (int) rOpen.ioUnit;
    }

    public void close() throws StyxException {
        if (mFID == Constants.NOFID) {
            return;
        }
        // send Tclunk
        final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFID);
        var feature = mTransmitter.sendMessage(tClunk, mClientId, mTimeout);
        feature.getResult();

        mFID = Constants.NOFID;
    }

    public List<StyxStat> listStat()
            throws StyxException {
        if (!isDirectory())
            return Collections.emptyList();
        var tempFID = getCloneFID();
        var iounit = open(ModeType.OREAD, tempFID);
        var stats = new ArrayList<StyxStat>();
        try {
            var is = new StyxFileBufferedInputStream(mTransmitter, tempFID, iounit, mClientId);
            var sis = new StyxDataInputStream(is);
            while (true) {
                stats.add(mDI.getDataDeserializer().deserializeStat(sis));
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

    /**
     * Open input stream to this file.
     *
     * @return input stream
     */
    public StyxFileBufferedInputStream openForRead()
            throws StyxException {
        long tempFID = getCloneFID();
        int iounit = open(ModeType.OREAD, tempFID);
        return new StyxFileBufferedInputStream(mTransmitter, tempFID, iounit, mClientId);
    }

    /**
     * Get unbuffered input stream to this file.
     *
     * @return unbuffered input stream
     */
    public StyxUnbufferedInputStream openForReadUnbuffered() throws StyxException {
        long tempFID = getCloneFID();
        int iounit = open(ModeType.OREAD, tempFID);
        return new StyxUnbufferedInputStream(tempFID, mTransmitter, iounit, mClientId);
    }

    public OutputStream openForWrite()
            throws StyxException {
        return new BufferedOutputStream(openForWriteUnbuffered());
    }

    public StyxUnbufferedOutputStream openForWriteUnbuffered()
            throws StyxException {
        long clonedFID = getCloneFID();
        return new StyxUnbufferedOutputStream(clonedFID,
                mTransmitter,
                mClientId,
                open(ModeType.OWRITE, clonedFID)
        );
    }

    public void create(long permissions) throws StyxException {
        // reserve FID
        long tempFID = sendWalkMessage(mParentFID, "");
        var tCreate = new StyxTCreateMessage(tempFID, mPath, permissions, ModeType.OWRITE);
        mTransmitter
            .sendMessage(tCreate, mClientId, mTimeout)
//                .exceptionally()
            .getResult();
        // TODO reuse FID
//        mFID = tempFID;
        // close temp FID
        var tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, tempFID);
        mTransmitter
            .sendMessage(tClunk, mClientId, mTimeout)
            .getResult();
//        mRecipient.getPolls().releaseFID(tCreate);
    }

    public boolean exists() throws StyxException {
        return (getFID() != Constants.NOFID);
    }

    /**
     * Delete file or empty folder
     */
    public void delete()
            throws StyxException {
        delete(false);
    }

    public void delete(boolean recursive)
            throws StyxException {
        if (recursive && this.isDirectory()) {
            for (var stat : listStat()) {
                var file = new StyxFile(stat.name(),
                        mFID,
                        mClientId,
                        mTransmitter,
                        mTimeout,
                        mDI);
                file.delete(true);
            }
        }
        long fid = getFID();
        mFID = Constants.NOFID;
        var tRemove = new StyxTMessageFID(MessageType.Tremove, MessageType.Rremove, fid);
        mTransmitter
            .sendMessage(tRemove, mClientId, mTimeout)
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
        mTransmitter.sendMessage(tWStat, mClientId, mTimeout).getResult();
    }

    public void mkdir(long permissions) throws InterruptedException, StyxException {
        permissions = permissions & FileMode.PERMISSION_BITMASK | FileMode.Directory;
        StyxTCreateMessage tCreate = new StyxTCreateMessage(mParentFID, getName(), permissions, ModeType.OREAD);
        mTransmitter.sendMessage(tCreate, mClientId, mTimeout).getResult();
    }

    public boolean checkFileMode(long mode)
            throws StyxException {
        StyxStat stat = getStat();
        return (mode & stat.mode()) != 0;
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
        StringBuilder builder = new StringBuilder(mPath);
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
        long newFID = mDI.getClientsRepo().getFidPoll(mClientId).getFreeItem();
        final StyxTWalkMessage tWalk = new StyxTWalkMessage(parentFID,
                newFID, StyxSerializerImpl.splitPath(path));
        final var feature = mTransmitter.sendMessage(tWalk, mClientId, mTimeout);
        final StyxMessage rWalk = feature.getResult();
        if (rWalk.getType() == MessageType.Rerror)
            throw StyxErrorMessageException.newInstance(((StyxRErrorMessage) rWalk).mError + " at " + mPath);
        if (((StyxRWalkMessage) rWalk).qidList.size() != tWalk.getPathLength())
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
        final var rMessage = mTransmitter.<StyxRStatMessage>sendMessage(
                new StyxTMessageFID(MessageType.Tstat, MessageType.Rstat, getFID()),
                mClientId,
                mTimeout).getResult();
        return rMessage.stat;
    }

    public StyxFile walk(String path) throws StyxException {
        return new StyxFile(
                path,
                getFID(),
                mClientId,
                mTransmitter,
                mTimeout,
                mDI
        );
    }
}
