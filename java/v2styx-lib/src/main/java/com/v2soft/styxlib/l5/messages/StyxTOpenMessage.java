package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class StyxTOpenMessage extends StyxTMessageFID {
    public final int mode;

    public StyxTOpenMessage(long fid, int mode) {
        super(MessageType.Topen, MessageType.Ropen, fid);
        this.mode = mode;
    }

    @Override
    public String toString() {
        return String.format("%s\nMode: %d",
                super.toString(), mode);
    }
}
