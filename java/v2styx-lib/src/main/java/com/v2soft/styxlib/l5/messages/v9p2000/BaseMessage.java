package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.dev.MetricsAndStats;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;

public class BaseMessage implements StyxMessage {
    protected int mTag;
    public final int type;
    public final StyxQID mQID;
    protected long fid;

    protected BaseMessage(
            int type,
            int tag,
            StyxQID qid,
            long fid) {
        MetricsAndStats.newStyxMessage++;
        this.type = type;
        mTag = tag;
        mQID = qid;
        this.fid = fid;
    }

    @Override
    public int getTag() {
        return mTag;
    }

    public void setTag(int tag) {
        mTag = (tag & 0xFFFF);
    }

    @Override
    public int getType() {
        return type;
    }

    public StyxQID getQID() {
        return mQID;
    }

    public long getFID() {
        return fid;
    }
}
