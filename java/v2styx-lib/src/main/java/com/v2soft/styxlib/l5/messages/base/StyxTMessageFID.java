package com.v2soft.styxlib.l5.messages.base;

public class StyxTMessageFID extends StyxTMessage {
    public final long mFID;

    public StyxTMessageFID(int type, long fid) {
        super(type);
        mFID = fid;
    }

    public long getFID() {
        return mFID;
    }
}
