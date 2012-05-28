package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.types.ULong;

public class StyxTWriteMessage extends StyxTMessage {
    private long mFID;
    private ULong mOffset;
    private byte[] mData;
    private int mDataOffset;
    private int mDataLength;

    public StyxTWriteMessage(long fid, ULong offset, byte [] data, int dataOffset, int dataLength) 
            throws IOException {
        super(MessageType.Twrite);
        mFID = fid;
        mOffset = offset;
        mData = data;
        mDataLength = dataLength;
    }
    // ===========================================================================
    // Styx message methods
    // ===========================================================================

    @Override
    public void load(IStyxDataReader input) throws IOException {
        mFID = input.readUInt32();
        mOffset = input.readUInt64();
        mDataLength = (int)input.readUInt32();
        mDataOffset = 0;
        mData = new byte[mDataLength];
        input.read(mData, 0, mDataLength);
    }
    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt32(mFID);
        output.writeUInt64(mOffset);
        output.writeUInt32(mDataLength);
        output.write(mData, mDataOffset, mDataLength);        
    }

    @Override
    protected String internalToString() {
        if ( Config.LOG_DATA_FIELDS ) {
            return String.format("FID: %d\nOffset: %s\nData: %s",
                    getFID(), getOffset().toString(), StyxMessage.toString(getData()));
        } else {
            return String.format("FID: %d\nOffset: %d",
                    getFID(), getOffset().asLong());
        }
    }    
    // ===========================================================================
    // Getters
    // ===========================================================================
    public long getFID(){return mFID;}
    public ULong getOffset(){return mOffset;}
    public byte[] getData() {
        if (mData == null)
            return new byte[0];
        return mData;
    }
    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 16
                + mDataLength;
    }
    
    public int getDataLength() {
        return mDataLength;
    }
    @Override
    protected MessageType getRequiredAnswerType() {
        return MessageType.Rwrite;
    }    
    // ===========================================================================
    // Setters
    // ===========================================================================
    public void setData(byte [] data) {mData = data;}
}
