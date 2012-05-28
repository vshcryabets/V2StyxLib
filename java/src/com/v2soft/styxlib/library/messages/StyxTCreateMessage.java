package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTCreateMessage extends StyxTMessage {
    private long mFID;
    private String mName;
    private long mPermissions;
    private int mMode;

    public StyxTCreateMessage(long fid, String name, long permissions, int mode)
    {
        super(MessageType.Tcreate);
        mFID = fid;
        mName = name;
        mPermissions = permissions;
        mMode = mode;
    }

    @Override
    public void load(IStyxDataReader input) throws IOException {
        mFID = input.readUInt32();
        mName = input.readUTFString();
        mPermissions = input.readUInt32();
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
        return super.getBinarySize() + 9
                + StyxMessage.getUTFSize(getName());
    }

    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt32(getFID());
        output.writeUTFString(getName());
        output.writeUInt32(getPermissions());
        output.writeUInt8((short) mMode);  
    }

    @Override
    protected String internalToString() {
        return String.format("FID: %d\nName: %s\nPermissions: %d\nMode: %d", 
                getFID(), getName(), getPermissions(), mMode);
    }

    @Override
    protected MessageType getRequiredAnswerType() {
        return MessageType.Rcreate;
    }

}
