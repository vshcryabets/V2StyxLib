package com.v2soft.styxlib.messages;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxTWriteMessage extends StyxTMessageFID {
    private long mOffset;
    private byte[] mData;
    private int mDataOffset;
    private int mDataLength;

    public StyxTWriteMessage(long fid, long offset, byte [] data, int dataOffset, int dataLength)
            throws IOException {
        super(MessageType.Twrite, MessageType.Rwrite, fid);
        mOffset = offset;
        mData = data;
        mDataLength = dataLength;
        mDataOffset = dataOffset;
    }
    // ===========================================================================
    // Styx message methods
    // ===========================================================================
    @Override
    public void load(IStyxDataReader input) throws IOException {
        super.load(input);
        mOffset = input.readUInt64();
        mDataLength = (int)input.readUInt32();
        mDataOffset = 0;
        mData = new byte[mDataLength];
        MetricsAndStats.byteArrayAllocationTWrite++;
        input.read(mData, 0, mDataLength);
    }
    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt64(mOffset);
        output.writeUInt32(mDataLength);
        output.write(mData, mDataOffset, mDataLength);
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
    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 12
                + mDataLength;
    }

    public int getDataLength() {
        return mDataLength;
    }
    // ===========================================================================
    // Setters
    // ===========================================================================
    public void setData(byte [] data) {mData = data;}
}
