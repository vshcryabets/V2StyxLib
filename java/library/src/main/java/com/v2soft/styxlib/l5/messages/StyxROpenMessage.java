package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.serialization.IStyxDataReader;
import com.v2soft.styxlib.l5.serialization.BufferWritter;
import com.v2soft.styxlib.l5.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.StyxQID;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxROpenMessage extends StyxRSingleQIDMessage {
    private long mIOUnit;

    public StyxROpenMessage(int tag, StyxQID qid, long iounit, boolean create) {
        super(( create ? MessageType.Rcreate : MessageType.Ropen ), tag, qid);
        mIOUnit = iounit;
    }

    @Override
    public void load(IStyxDataReader stream) throws IOException {
        super.load(stream);
        mIOUnit = stream.readUInt32();
    }

    public long getIOUnit() {
        return mIOUnit;
    }

    public void setIOUnit(long iounit) {
        mIOUnit = iounit;
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 4;
    }

    @Override
    public String toString() {
        return String.format("%s\nIOUnit: %d",
                super.toString(),
                getIOUnit());
    }
}
