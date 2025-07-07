package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.exceptions.StyxNotAuthorizedException;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.utils.StyxSessionDI;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/**
 * In-Memory file
 *
 * @author vshcryabets@gmail.com
 */
public class MemoryStyxFile implements IVirtualStyxFile {
    protected final StyxSessionDI mDI;
    protected final String mName;
    protected StyxQID mQID;
    protected StyxStat mStat;
    protected static final int ALL_MODES = 0x000001FF;

    public MemoryStyxFile(String name,
                          StyxSessionDI di) {
        if (name == null) {
            throw new NullPointerException("Filename is null");
        }
        mDI = di;
        mName = name;
        mQID = new StyxQID(QidType.QTFILE, 0, mName.hashCode());
    }

    @Override
    public StyxQID getQID() {
        return mQID;
    }

    @Override
    public StyxStat getStat() {
        if (mStat == null) {
            mStat = new StyxStat((short) 0,
                    1,
                    mQID,
                    getMode(),
                    getAccessTime(),
                    getModificationTime(),
                    getLength(),
                    mName,
                    getOwnerName(),
                    getGroupName(),
                    getModificationUser());
        }
        return mStat;
    }

    @Override
    public int getMode() {
        return ALL_MODES;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Date getAccessTime() {
        return new Date();
    }

    @Override
    public Date getModificationTime() {
        return new Date();
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public String getOwnerName() {
        return "nobody";
    }

    @Override
    public String getGroupName() {
        return "nobody";
    }

    @Override
    public String getModificationUser() {
        return "nobody";
    }

    @Override
    public boolean open(int clientId, int mode) throws StyxException {
        if (!mDI.getIsClientAuthorizedUseCase().isClientAuthorized(clientId)) {
            throw new StyxNotAuthorizedException();
        }
        return ( ( mode == ModeType.OREAD ) ||
                ( mode == ModeType.OWRITE ) ||
                ( mode == ModeType.ORDWR ) );
    }

    @Override
    public IVirtualStyxFile walk(int clientId, Queue<String> pathElements, List<StyxQID> qids)
            throws StyxException {
        if (!mDI.getIsClientAuthorizedUseCase().isClientAuthorized(clientId)) {
            throw new StyxNotAuthorizedException();
        }
        return this;
    }

    public int write(int clientId, byte[] data,
                     long offset) throws StyxErrorMessageException {
        return 0;
    }

    @Override
    public int read(int clientId, byte[] outbuffer, long offset,
                     int count) throws StyxException {
        if (!mDI.getIsClientAuthorizedUseCase().isClientAuthorized(clientId)) {
            throw new StyxNotAuthorizedException();
        }
        return 0;
    }

    @Override
    public void close(int clientId) {
    }

    @Override
    public void onConnectionOpened(int clientId) {
        // ok, nothing to do
    }

    @Override
    public StyxQID create(int clientId, String name, long permissions, int mode)
            throws StyxErrorMessageException {
        throw StyxErrorMessageException.newInstance("Can't create file, this is read-only file system.");
    }

    @Override
    public boolean delete(int clientId) {
        close(clientId);
        return false;
    }

    @Override
    public void release() throws IOException {

    }
}
