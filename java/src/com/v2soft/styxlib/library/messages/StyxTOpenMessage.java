package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxTOpenMessage extends StyxTMessage {
    private long mFID;
    private ModeType mMode;

    public StyxTOpenMessage()
    {
        this(NOFID, ModeType.OREAD);
    }

    public StyxTOpenMessage(long fid, ModeType mode)
    {
        super(MessageType.Topen);
        mFID = fid;
        mMode = mode;
    }

    public StyxTOpenMessage(int tag)
    {
        this(tag, NOFID, ModeType.OREAD);
    }

    public StyxTOpenMessage(int tag, long fid, ModeType mode) {
        super(MessageType.Topen, tag);
        mFID = fid;
        mMode = mode;
    }

    @Override
    public void load(StyxBufferOperations input) 
            throws IOException  {
        setFID(input.readUInt32());
        setMode(ModeType.factory(input.readUInt8()));
    }

    public long getFID()
    {
        return mFID;
    }

    public void setFID(long fid)
    {
        mFID = fid;
    }

    public ModeType getMode()
    {
        return mMode;
    }

    public void setMode(ModeType mode)
    {
        mMode = mode;
    }

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 5;
    }

    @Override
    public void writeToBuffer(StyxBufferOperations output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt(getFID());
        output.writeUByte((short)getMode().getByte());     
    }

    @Override
    protected String internalToString() {
        return String.format("FID: %d\nMode: %s", 
                getFID(), getMode().toString());
    }

    @Override
    protected MessageType getRequiredAnswerType() {
        return MessageType.Ropen;
    }

}
