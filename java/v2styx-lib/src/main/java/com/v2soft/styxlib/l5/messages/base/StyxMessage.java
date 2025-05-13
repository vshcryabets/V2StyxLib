package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.dev.MetricsAndStats;

public class StyxMessage {
    private int mTag;
    public final int type;

    public StyxMessage(int type, int tag) {
        MetricsAndStats.newStyxMessage++;
        this.type = type;
        mTag = tag;
    }

    public int getTag(){
        return mTag;
    }

    public void setTag(int tag){
        mTag = (tag & 0xFFFF);
    }

    @Override
    public String toString() {
        return String.format("Type: %d\tTag: %d",
                type, getTag());
    }
}
