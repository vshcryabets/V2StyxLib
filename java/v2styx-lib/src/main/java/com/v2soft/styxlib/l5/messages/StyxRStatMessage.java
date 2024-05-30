package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxStat;

public class StyxRStatMessage extends StyxMessage {
    public final StyxStat stat;

    public StyxRStatMessage(int tag, StyxStat stat) {
        super(MessageType.Rstat, tag);
        this.stat = stat;
    }

    @Override
    public String toString() {
        return String.format("%s\nStat: %s", super.toString(), stat.toString());
    }
}
