package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.QID;

public class StyxTOpenMessage extends StyxTMessage {
    public final int mode;

    protected StyxTOpenMessage(long fid, int mode) {
        super(MessageType.Topen, QID.EMPTY, fid, 0, null);
        this.mode = mode;
    }

    @Override
    public String toString() {
        return String.format("%s\nMode: %d",
                super.toString(), mode);
    }
}
