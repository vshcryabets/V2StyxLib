package com.v2soft.styxlib.messages;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.messages.base.structs.StyxStat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxRStatMessage extends StyxMessage {
    private StyxStat mStat;

    public StyxRStatMessage(int tag, StyxStat stat) {
        this(tag);
        mStat = stat;
    }

    public StyxRStatMessage(int tag) {
        super(MessageType.Rstat, tag);
    }

    @Override
    public void load(IStyxDataReader input) throws IOException {
        super.load(input);
        input.readUInt16();
        mStat = new StyxStat(input);
    }

    public StyxStat getStat() {
        if (mStat == null)
            return StyxStat.EMPTY;
        return mStat;
    }

    public void setStat(StyxStat stat) {
        mStat = stat;
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize()
                + 2 + getStat().getSize();
    }

    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt16(mStat.getSize());
        mStat.writeBinaryTo(output);
    }

    @Override
    public String toString() {
        return String.format("%s\nStat: %s", super.toString(), getStat().toString());
    }
}
