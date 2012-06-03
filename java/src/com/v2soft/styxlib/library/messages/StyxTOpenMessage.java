package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTOpenMessage extends StyxTMessage {
    private long mFID;
    private int mMode;

    public StyxTOpenMessage(long fid, int mode) {
        super(MessageType.Topen);
        mFID = fid;
        mMode = mode;
    }

    @Override
    public void load(IStyxDataReader input) 
            throws IOException  {
        mFID = input.readUInt32();
        mMode = input.readUInt8();
    }

    public long getFID()
    {
        return mFID;
    }

    public void setFID(long fid)
    {
        mFID = fid;
    }

    public int getMode()
    {
        return mMode;
    }

    public void setMode(int mode)
    {
        mMode = mode;
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 5;
    }

    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt32(getFID());
        output.writeUInt8((short) mMode);     
    }

    @Override
    protected String internalToString() {
        return String.format("FID: %d\nMode: %d", 
                getFID(), mMode);
    }

    @Override
    protected MessageType getRequiredAnswerType() {
        return MessageType.Ropen;
    }

}
