package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.IOException;

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
    public void load(IBufferReader input) throws IOException {
        super.load(input);
        input.readUInt16();
        mStat = new StyxStat(input);
    }

    public StyxStat getStat() {
        if (mStat == null)
            return StyxStat.EMPTY;
        return mStat;
    }

    @Override
    public String toString() {
        return String.format("%s\nStat: %s", super.toString(), getStat().toString());
    }
}
