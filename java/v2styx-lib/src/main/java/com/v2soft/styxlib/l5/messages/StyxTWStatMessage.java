package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.structs.StyxStat;

public class StyxTWStatMessage extends StyxTMessageFID {
    public final StyxStat stat;

    public StyxTWStatMessage(long fid, StyxStat stat) {
        super(MessageType.Twstat, MessageType.Rwstat, fid);
        this.stat = stat;
    }
}
