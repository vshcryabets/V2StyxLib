package com.v2soft.styxlib.library.server.vfs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.types.ULong;

/**
 * In-Memory file
 * @author vshcryabets@gmail.com
 *
 */
public class MemoryStyxFile implements IVirtualStyxFile {
    protected String mName;
    protected StyxQID mQID;
    protected StyxStat mStat;

    public MemoryStyxFile(String name) {
        if ( name == null ) {
            throw new NullPointerException("Filename is null");
        }
        mName = name;
        mQID = new StyxQID(QIDType.QTFILE, 0, new ULong(mName.hashCode()));
    }

    @Override
    public StyxQID getQID() {return mQID;}
    @Override
    public StyxStat getStat() {
        if ( mStat == null ) {
            mStat = new StyxStat((short)0, 
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
        return 0x000001FF;
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
    public boolean open(ClientState client, int mode) throws IOException {
        return ( (mode == ModeType.OREAD) || 
                (mode == ModeType.OWRITE) || 
                (mode==ModeType.ORDWR) );
    }

    @Override
    public IVirtualStyxFile walk(Iterator<String> pathElements, List<StyxQID> qids) 
            throws StyxErrorMessageException {
        return this;
    }

    public int write(ClientState client, byte[] data, ULong offset) throws StyxErrorMessageException {
        return 0;
    }

    @Override
    public long read(ClientState client, byte[] outbuffer, ULong offset, long count) throws StyxErrorMessageException {
        return 0;
    }

    @Override
    public void close(ClientState client) {
    }

    // ==============================================================
    // Abstract methods
    // ==============================================================
    protected int stringReply(String value, byte[] buffer, String charset) 
            throws UnsupportedEncodingException {
        byte[] bytes = value.getBytes(charset);
        System.arraycopy(bytes, 0, buffer, 0, bytes.length);
        return bytes.length;
    }
    protected int stringReplyWithOffset(String value, byte[] buffer, String charset, 
            long offset, int count) throws UnsupportedEncodingException {
        return byteReplyWithOffset(value.getBytes(charset), buffer, offset, count);
    }
    protected int byteReplyWithOffset(byte[] reply, byte[] buffer, long offset, int count) {
        long start = offset;
        if ( start >= reply.length ) {
            return 0;
        } else {
            if ( start+count > reply.length ) {
                count = (int) (reply.length-start);
            }
            System.arraycopy(reply, 0, buffer, 0, count);
            return count;
        }
    }

    @Override
    public void onConnectionClosed(ClientState state) {
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
    public boolean delete(ClientState client) {
        close(client);
        return false;
    }
}
