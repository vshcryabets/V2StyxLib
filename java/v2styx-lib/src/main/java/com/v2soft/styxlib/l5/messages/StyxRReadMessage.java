package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.BufferReader;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;

public class StyxRReadMessage extends StyxMessage {
    private byte[] mData;
    private int mDataLength;

    public StyxRReadMessage(int tag, byte[] data, int length) {
        super(MessageType.Rread, tag);
        mData = data;
        mDataLength = length;
    }

    @Override
    public void load(BufferReader input)
            throws IOException  {
        super.load(input);
        mDataLength = (int)input.readUInt32();
        mData = new byte[mDataLength];
        MetricsAndStats.byteArrayAllocationRRead++;
        input.read(mData, 0, mDataLength);
    }

    public int getDataLength() {return mDataLength;}

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 4
                + mDataLength;
    }

    @Override
    public String toString() {
        if ( Config.LOG_DATA_FIELDS ) {
            return String.format("%s\nData Length:%d\nData: %s",
                    super.toString(),
                    mData.length,
                    StyxMessage.toString(mData));
        } else {
            return String.format("%s\nData Length:%d",
                    super.toString(),
                    mData.length);
        }
    }
    public byte[] getDataBuffer() {
        return mData;
    }
}
