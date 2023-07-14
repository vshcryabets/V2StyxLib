package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.BufferReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;

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
    public void load(BufferReader input) throws IOException {
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

    public long getPermissions()
    {
        return mPermissions;
    }

    public int getMode(){return mMode;}

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
