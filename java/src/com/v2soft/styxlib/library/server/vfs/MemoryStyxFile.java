package com.v2soft.styxlib.library.server.vfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.v2soft.styxlib.library.core.StyxByteBuffer;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.types.ULong;

public class MemoryStyxFile implements IVirtualStyxFile {
    private String mName;
    
    public MemoryStyxFile(String name) {
        if ( name == null ) throw new NullPointerException("Filename is null");
        mName = name;
    }

    @Override
    public StyxQID getQID() {
        return new StyxQID(QIDType.QTFILE, 0, new ULong(this.hashCode()));
    }

    @Override
    public StyxStat getStat() {
        StyxStat result = new StyxStat((short)0, 
                1, 
                getQID(), 
                getMode(),
                getAccessTime(), 
                getModificationTime(), 
                getLength(), 
                getName(), 
                getOwnerName(), 
                getGroupName(), 
                getModificationUser());
        return result;
    }

    @Override
    public int getMode() {
        return 0x800001FF;
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
    public boolean open(ClientState client, ModeType mode) throws IOException {
        return false;
    }

    @Override
    public long read(ClientState client, byte[] outbuffer, ULong offset, long count) throws StyxErrorMessageException {
        return 0;
    }

    @Override
    public void close(ClientState client) {
    }

}
