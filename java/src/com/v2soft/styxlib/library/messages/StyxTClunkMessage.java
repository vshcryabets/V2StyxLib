package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTClunkMessage extends StyxTMessage {
    private long mFID;

    public StyxTClunkMessage(long fid)
    {
        super(MessageType.Tclunk);
        mFID = fid;
    }

    @Override
    public void load(StyxDataReader input) 
            throws IOException  {
        setFID(input.readUInt32());
    }

    public long getFID()
    {
        return mFID;
    }

    public void setFID(long fid)
    {
        mFID = fid;
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 4;
    }

    @Override
    public void writeToBuffer(StyxDataReader output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt(getFID());
    }

    @Override
    protected String internalToString() {
        return String.format("FID: %d", getFID());
    }

    @Override
    protected MessageType getRequiredAnswerType() {
        return MessageType.Rclunk;
    }

}
