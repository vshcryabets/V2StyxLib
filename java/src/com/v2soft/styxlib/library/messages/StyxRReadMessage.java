package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxRReadMessage extends StyxMessage {
    private byte[] mData;
    private int mDataLength;

    public StyxRReadMessage(int tag, byte[] data, int length) {
        super(MessageType.Rread, tag);
        mData = data;
        mDataLength = length;
    }

    @Override
    public void load(StyxDataReader input) 
            throws IOException  {
        mDataLength = (int)input.readUInt32();
        mData = new byte[mDataLength];
        input.read(mData, 0, mDataLength);
    }

    public int getDataLength() {return mDataLength;}

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 4
                + getDataLength();
    }

    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        int length = getDataLength();
        output.writeUInt32(length);
        if ( length > 0 ) {
            output.write(mData, 0, mDataLength);
        }
    }

    @Override
    protected String internalToString() {
        if ( Config.LOG_DATA_FIELDS ) {
            return String.format("Data Length:%d\nData: %s",
                    mData.length,
                    StyxMessage.toString(mData));
        } else {
            return String.format("Data Length:%d",
                    mData.length);
        }
    }

    public byte[] getDataBuffer() {
        return mData;
    }

}
