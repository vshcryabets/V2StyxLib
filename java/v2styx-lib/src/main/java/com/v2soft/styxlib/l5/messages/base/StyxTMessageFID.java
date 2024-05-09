package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.serialization.IBufferReader;

import java.io.IOException;

public class StyxTMessageFID extends StyxTMessage {
    private long mFID;

    public StyxTMessageFID(MessageType type, MessageType answer, long fid) {
        super(type, answer);
        mFID = fid;
    }

    public long getFID() {
        return mFID;
    }

    @Override
    public void load(IBufferReader input)
            throws IOException  {
        super.load(input);
        mFID = input.readUInt32();
    }

    @Override
    public String toString() {
        String result = super.toString();
        return String.format("%s\tFID: %d", result, getFID());
    }
}
