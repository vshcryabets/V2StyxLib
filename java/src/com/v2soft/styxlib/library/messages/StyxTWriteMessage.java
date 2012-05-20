package com.v2soft.styxlib.library.messages;

import java.io.IOException;
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
    // ===========================================================================
    // Styx message methods
    // ===========================================================================

    @Override
    public void load(StyxBufferOperations input) throws IOException {
        mFID = input.readUInt32();
        mOffset = input.readUInt64();
        int count = (int)input.readUInt32();
        mData = new byte[count];
        input.read(mData, 0, count);
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
                + getDataLength();
    }
    
    public int getDataLength()
    {
        if (mData == null)
            return 0;
        return mData.length;
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
