package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.BufferReader;

import java.io.IOException;

public class StyxTOpenMessage extends StyxTMessageFID {
    private int mMode;

    public StyxTOpenMessage(long fid, int mode) {
        super(MessageType.Topen, MessageType.Ropen, fid);
        mMode = mode;
    }

    @Override
    public void load(BufferReader input)
            throws IOException  {
        super.load(input);
        mMode = input.readUInt8();
    }

    public int getMode()
    {
        return mMode;
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 1;
    }

    @Override
    public String toString() {
        return String.format("%s\nMode: %d",
                super.toString(), mMode);
    }
}
