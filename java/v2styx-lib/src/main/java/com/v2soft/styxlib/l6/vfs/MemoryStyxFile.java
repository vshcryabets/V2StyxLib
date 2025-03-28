package com.v2soft.styxlib.l6.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.enums.ModeType;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.server.ClientDetails;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * In-Memory file
 *
 * @author vshcryabets@gmail.com
 */
public class MemoryStyxFile implements IVirtualStyxFile {
    protected String mName;
    protected StyxQID mQID;
    protected StyxStat mStat;
    protected static final int ALL_MODES = 0x000001FF;

    public MemoryStyxFile(String name) {
        if (name == null) {
            throw new NullPointerException("Filename is null");
        }
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
        return ( ( mode == ModeType.OREAD ) ||
                ( mode == ModeType.OWRITE ) ||
                ( mode == ModeType.ORDWR ) );
    }

    @Override
    public IVirtualStyxFile walk(Iterator<String> pathElements, List<StyxQID> qids)
            throws StyxErrorMessageException {
        return this;
    }

    public int write(int clientId, byte[] data,
                     long offset) throws StyxErrorMessageException {
        return 0;
    }

    @Override
    public int read(int clientId, byte[] outbuffer, long offset,
                     int count) throws StyxErrorMessageException {
        return 0;
    }

    @Override
    public void close(int clientId) {
    }

    protected int stringReply(String value, byte[] buffer, Charset charset) {
        byte[] bytes = value.getBytes(charset);
        System.arraycopy(bytes, 0, buffer, 0, bytes.length);
        return bytes.length;
    }

    protected int stringReplyWithOffset(String value, byte[] buffer, Charset charset,
                                        long offset, int count) {
        return byteReplyWithOffset(value.getBytes(charset), buffer, offset, count);
    }

    protected int byteReplyWithOffset(byte[] reply, byte[] buffer, long offset, int count) {
        if (offset >= reply.length) {
            return 0;
        } else {
            if (offset + count > reply.length) {
                count = (int) ( reply.length - offset );
            }
            System.arraycopy(reply, 0, buffer, 0, count);
            return count;
        }
    }

    @Override
    public void onConnectionOpened(int clientId) {
        // ok, nothing to do
    }

    @Override
    public StyxQID create(String name, long permissions, int mode)
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
