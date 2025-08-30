package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.StyxStat;

public class StyxTWStatMessage extends StyxTMessage {
    public final StyxStat stat;

    protected StyxTWStatMessage(long fid, StyxStat stat) {
        super(MessageType.Twstat, null, fid, 0);
        this.stat = stat;
    }
}
