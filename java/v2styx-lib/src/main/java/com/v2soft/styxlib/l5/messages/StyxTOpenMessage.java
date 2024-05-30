package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IBufferReader;

import java.io.IOException;

public class StyxTOpenMessage extends StyxTMessageFID {
    private int mMode;

    public StyxTOpenMessage(long fid, int mode) {
        super(MessageType.Topen, MessageType.Ropen, fid);
        mMode = mode;
    }

    public int getMode()
    {
        return mMode;
    }

    @Override
    public String toString() {
        return String.format("%s\nMode: %d",
                super.toString(), mMode);
    }
}
