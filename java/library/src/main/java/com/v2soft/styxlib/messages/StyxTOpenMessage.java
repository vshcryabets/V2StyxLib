package com.v2soft.styxlib.messages;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxTOpenMessage extends StyxTMessageFID {
    private int mMode;

    public StyxTOpenMessage(long fid, int mode) {
        super(MessageType.Topen, MessageType.Ropen, fid);
        mMode = mode;
    }

    @Override
    public void load(IStyxDataReader input)
            throws IOException  {
        super.load(input);
        mMode = input.readUInt8();
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
        return super.getBinarySize() + 1;
    }

    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws IOException {
        super.writeToBuffer(output);
        output.writeUInt8((short) mMode);
    }

    @Override
    public String toString() {
        return String.format("%s\nMode: %d",
                super.toString(), mMode);
    }
}
