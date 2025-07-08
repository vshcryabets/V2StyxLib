package com.v2soft.styxlib.l5.messages.base;

public class StyxTMessageFID extends StyxTMessage {
    private long mFID;

    public StyxTMessageFID(int type, int answer, long fid) {
        super(type, answer);
        mFID = fid;
    }

    public long getFID() {
        return mFID;
    }
}
