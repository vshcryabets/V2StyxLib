package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.structs.StyxStat;

public class StyxTWStatMessage extends StyxTMessageFID {
    public final StyxStat stat;

    protected StyxTWStatMessage(long fid, StyxStat stat) {
        super(MessageType.Twstat, fid);
        this.stat = stat;
    }
}
