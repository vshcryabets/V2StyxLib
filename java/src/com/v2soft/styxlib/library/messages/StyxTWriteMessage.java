package com.v2soft.styxlib.library.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;
import com.v2soft.styxlib.library.types.ULong;

public class StyxTWriteMessage extends StyxTMessage {
    private long mFID;
    private ULong mOffset;
    private byte[] mData;

    public StyxTWriteMessage(long fid, ULong offset, byte [] data) 
            throws IOException {
        super(MessageType.Twrite);
        mFID = fid;
        mOffset = offset;
        mData = data;
    }

    @Override
    public void load(StyxBufferOperations input) throws IOException {
        mFID = input.readUInt32();
        mOffset = input.readUInt64();
        int count = (int)input.readUInt32();
        mData = new byte[count];
        input.read(mData, 0, count);
    }

    public long getFID()
    {
        return mFID;
    }

    public void setFID(long fid)
    {
        mFID = fid;
    }

    public ULong getOffset()
    {
        return mOffset;
    }

    public void setOffset(ULong offset)
    {
        mOffset = offset;
    }

    private byte[] getData() {
        if (mData == null)
            return new byte[0];
        return mData;
    }

    public InputStream getDataStream()
    {
        return new ByteArrayInputStream(getData());
    }

    public int getDataLength()
    {
        if (mData == null)
            return 0;
        return mData.length;
    }

    public void setData(byte [] data) {
        mData = data;
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 16
                + getDataLength();
    }

    @Override
    public void writeToBuffer(StyxBufferOperations output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt(mFID);
        output.writeUInt64(mOffset);
        output.writeUInt(getDataLength());
        output.write(getData());        
    }

    @Override
    protected String internalToString() {
        if ( Config.LOG_DATA_FIELDS ) {
            return String.format("FID: %d\nOffset: %s\nData: %s",
                    getFID(), getOffset().toString(), StyxMessage.toString(getData()));
        } else {
            return String.format("FID: %d\nOffset: %d",
                    getFID(), getOffset());
        }
    }

    @Override
    protected MessageType getRequiredAnswerType() {
        return MessageType.Rwrite;
    }

}
