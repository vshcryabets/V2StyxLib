package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.types.ULong;

public class StyxTWriteMessage extends StyxTMessageFID {
    private ULong mOffset;
    private byte[] mData;
    private int mDataOffset;
    private int mDataLength;

    public StyxTWriteMessage(long fid, ULong offset, byte [] data, int dataOffset, int dataLength) 
            throws IOException {
        super(MessageType.Twrite, MessageType.Rwrite, fid);
        mOffset = offset;
        mData = data;
        mDataLength = dataLength;
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
            return String.format("%s\nOffset: %s\nData: %s",
                    super.toString(), getOffset().toString(), StyxMessage.toString(getData()));
        } else {
            return String.format("%s\nOffset: %d",
                    super.toString(), getOffset().asLong());
        }
    }    
    // ===========================================================================
    // Getters
    // ===========================================================================
    public ULong getOffset(){return mOffset;}
    public byte[] getData() {
        if (mData == null)
            return new byte[0];
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
