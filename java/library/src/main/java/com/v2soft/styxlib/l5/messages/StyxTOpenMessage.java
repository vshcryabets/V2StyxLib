package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.io.IStyxDataReader;
import com.v2soft.styxlib.l5.io.IStyxDataWriter;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.enums.MessageType;

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
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt8((short) mMode);
    }

    @Override
    public String toString() {
        return String.format("%s\nMode: %d",
                super.toString(), mMode);
    }
}
