package com.v2soft.styxlib.messages.base;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.enums.MessageType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxTMessageFID extends StyxTMessage {
    private long mFID;
    // ===========================================================================
    // Constructor
    // ===========================================================================
    public StyxTMessageFID(MessageType type, MessageType answer, long fid) {
        super(type, answer);
        mFID = fid;
    }
    // ===========================================================================
    // Getters
    // ===========================================================================
    public long getFID() {
        return mFID;
    }
    // ===========================================================================
    // Setters
    // ===========================================================================
    public void setFID(long fid) {
        mFID = fid;
    }
    // ===========================================================================
    // Encoder/Decoder methods
    // ===========================================================================
    @Override
    public void load(IStyxDataReader input)
            throws IOException  {
        super.load(input);
        mFID = input.readUInt32();
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 4;
    }

    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt32(mFID);
    }

    @Override
    public String toString() {
        String result = super.toString();
        return String.format("%s\tFID: %d", result, getFID());
    }
}
