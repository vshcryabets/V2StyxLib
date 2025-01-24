package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;

public class StyxRReadMessage extends StyxMessage {
    private final byte[] mData;
    private final int mDataLength;

    public StyxRReadMessage(int tag, byte[] data, int length) {
        super(MessageType.Rread, tag);
        if (length < 0) {
            throw new IllegalArgumentException("length is negative " + length);
        }
        mData = data;
        mDataLength = length;
    }

    public int getDataLength() {return mDataLength;}

    @Override
    public String toString() {
        if ( Config.LOG_DATA_FIELDS ) {
            return String.format("%s\nData Length:%d\nData: %s",
                    super.toString(),
                    mDataLength,
                    StyxMessage.toString(mData));
        } else {
            return String.format("%s\nData Length:%d",
                    super.toString(),
                    mDataLength);
        }
    }
    public byte[] getDataBuffer() {
        return mData;
    }
}
