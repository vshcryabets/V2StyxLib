package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.IOException;

public class StyxTWStatMessage extends StyxTMessageFID {
    private StyxStat mStat;

    public StyxTWStatMessage(long fid, StyxStat stat) {
        super(MessageType.Twstat, MessageType.Rwstat, fid);
        mStat = stat;
    }

    @Override
    public void load(IBufferReader input) throws IOException {
        super.load(input);
        input.readUInt16();
        mStat = new StyxStat(input);
    }

    public StyxStat getStat() {
        return mStat;
    }

    @Override
    public String toString() {
        return String.format("%s\nStat: %s",
                super.toString(), getStat().toString());
    }
}
