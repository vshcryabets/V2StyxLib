package com.v2soft.styxlib.library.messages;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

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
                + StyxMessage.getUTFSize(getName());
    }

    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUTFString(getName());
        output.writeUInt32(getPermissions());
        output.writeUInt8((short) mMode);  
    }

    @Override
    public String toString() {
        return String.format("%s\nName: %s\nPermissions: %d\nMode: %d", 
                super.toString(), getName(), getPermissions(), mMode);
    }
}
