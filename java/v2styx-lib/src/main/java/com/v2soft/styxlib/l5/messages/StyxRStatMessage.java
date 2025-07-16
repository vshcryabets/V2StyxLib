package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.structs.StyxStat;

public class StyxRStatMessage extends BaseMessage {
    public final StyxStat stat;

    public StyxRStatMessage(int tag, StyxStat stat) {
        super(MessageType.Rstat, tag, null);
        this.stat = stat;
    }

    @Override
    public String toString() {
        return String.format("%s\nStat: %s", super.toString(), stat.toString());
    }
}
