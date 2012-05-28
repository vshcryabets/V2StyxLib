package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;

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
        //        int size = 
        input.readUInt16();
        mStat = new StyxStat(input);
    }

    public StyxStat getStat()
    {
        if (mStat == null)
            return StyxStat.EMPTY;
        return mStat;
    }

    public void setStat(StyxStat stat)
    {
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
    protected String internalToString() {
        return String.format("Stat: %s", getStat().toString());
    }

}
