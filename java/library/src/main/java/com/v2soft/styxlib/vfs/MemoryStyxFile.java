package com.v2soft.styxlib.vfs;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.messages.base.enums.ModeType;
import com.v2soft.styxlib.messages.base.enums.QIDType;
import com.v2soft.styxlib.messages.base.structs.StyxQID;
import com.v2soft.styxlib.messages.base.structs.StyxStat;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.types.ULong;

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
        mQID = new StyxQID(QIDType.QTFILE, 0, new ULong(mName.hashCode()));
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
    public ULong getLength() {
        return new ULong(0);
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
    public boolean open(ClientDetails clientDetails, int mode) throws IOException {
        return ( ( mode == ModeType.OREAD ) ||
                ( mode == ModeType.OWRITE ) ||
                ( mode == ModeType.ORDWR ) );
    }

    @Override
    public IVirtualStyxFile walk(Iterator<String> pathElements, List<StyxQID> qids)
            throws StyxErrorMessageException {
        return this;
    }

    public int write(ClientDetails clientDetails, byte[] data, ULong offset) throws StyxErrorMessageException {
        return 0;
    }

    @Override
    public long read(ClientDetails clientDetails, byte[] outbuffer, ULong offset, long count) throws StyxErrorMessageException {
        return 0;
    }

    @Override
    public void close(ClientDetails clientDetails) {
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
    public void onConnectionClosed(ClientDetails state) {
        // ok, nothing to do
    }

    @Override
    public void onConnectionOpened(ClientDetails client) {
        // ok, nothing to do
    }

    @Override
    public StyxQID create(String name, long permissions, int mode)
            throws StyxErrorMessageException {
        StyxErrorMessageException.doException(
                "Can't create file, this is read-only file system.");
        return null;
    }

    @Override
    public boolean delete(ClientDetails clientDetails) {
        close(clientDetails);
        return false;
    }

    @Override
    public void release() throws IOException {

    }
}
