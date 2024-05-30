package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;

public class StyxTWriteMessage extends StyxTMessageFID {
    private final long mOffset;
    private final byte[] mData;
    private final int mDataOffset;
    private final int mDataLength;

    public StyxTWriteMessage(long fid,
                             long offset,
                             byte [] data,
                             int dataOffset,
                             int dataLength) {
        super(MessageType.Twrite, MessageType.Rwrite, fid);
        mOffset = offset;
        mData = data;
        mDataLength = dataLength;
        mDataOffset = dataOffset;
    }

    @Override
    public String toString() {
        if ( Config.LOG_DATA_FIELDS ) {
            return String.format("%s\nOffset: %d\nData: %s",
                    super.toString(), getOffset(), StyxMessage.toString(getData()));
        } else {
            return String.format("%s\nOffset: %d",
                    super.toString(), getOffset());
        }
    }
    // ===========================================================================
    // Getters
    // ===========================================================================
    public long getOffset(){return mOffset;}
    public byte[] getData() {
        if (mData == null) {
            MetricsAndStats.byteArrayAllocation++;
            return new byte[0];
        }
        return mData;
    }

    public int getDataLength() {
        return mDataLength;
    }

    public int getDataOffset() {
        return mDataOffset;
    }
}
