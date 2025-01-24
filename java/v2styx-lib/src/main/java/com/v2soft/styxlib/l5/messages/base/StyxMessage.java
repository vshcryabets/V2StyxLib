package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;

public class StyxMessage {
    public static final int NOTAG  =      0xFFFF;
    public static final long NOFID = 0xFFFFFFFFL;

    private int mTag;
    private final int mType;

    public StyxMessage(int type, int tag) {
        MetricsAndStats.newStyxMessage++;
        mType = type;
        mTag = tag;
    }

    public static String toString(byte[] bytes) {
        if ( (bytes == null) || (bytes.length==0))
            return "-";
        final StringBuilder result = new StringBuilder();
        result.append(Integer.toHexString(((int)bytes[0])&0xFF));
        result.append(",");
        int count = bytes.length;
        for (int i=1; i<count; i++)
        {
            result.append(Integer.toHexString(((int)bytes[i])&0xFF));
            result.append(',');
        }
        return String.format("(%s)", result);
    }

    public int getType(){
        return mType;
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
                getType(), getTag());
    }
}
