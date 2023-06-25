package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.serialization.IStyxDataReader;
import com.v2soft.styxlib.l5.serialization.BufferWritter;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxTCreateMessage extends StyxTMessageFID {
    private String mName;
    private long mPermissions;
    private int mMode;

    public StyxTCreateMessage(long fid, String name, long permissions, int mode) {
        super(MessageType.Tcreate, MessageType.Rcreate, fid);
        mName = name;
        mPermissions = permissions;
        mMode = mode;
    }

    @Override
    public void load(IStyxDataReader input) throws IOException {
        super.load(input);
        mName = input.readUTFString();
        mPermissions = input.readUInt32();
        mMode = input.readUInt8();
    }

    public String getName()
    {
        if (mName == null)
            return "";
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public long getPermissions()
    {
        return mPermissions;
    }

    public void setPermissions(long permissions)
    {
        mPermissions = permissions;
    }

    public int getMode(){return mMode;}

    public void setMode(int mode){mMode = mode;}

    @Override
    public int getBinarySize() {
        return super.getBinarySize() + 5
                + UTF.getUTFSize(getName());
    }

    @Override
    public String toString() {
        return String.format("%s\nName: %s\nPermissions: %d\nMode: %d",
                super.toString(), getName(), getPermissions(), mMode);
    }
}
