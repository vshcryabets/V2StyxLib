package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxTCreateMessage extends StyxTMessage {
    private long mFID;
    private String mName;
    private long mPermissions;
    private ModeType mMode;


    public StyxTCreateMessage(int tag) {
        super(MessageType.Tcreate);
    }

    public StyxTCreateMessage(String name, long permissions)
    {
        this(NOFID, name, permissions, ModeType.OREAD);
    }

    public StyxTCreateMessage(long fid, String name, long permissions, ModeType mode)
    {
        super(MessageType.Tcreate);
        mFID = fid;
        mName = name;
        mPermissions = permissions;
        mMode = mode;
    }

    public StyxTCreateMessage(int tag, String name, long permissions)
    {
        this(tag, NOFID, name, permissions, ModeType.OREAD);
    }

    public StyxTCreateMessage(int tag, long fid, String name, long permissions, ModeType mode)
    {
        super(MessageType.Tcreate, tag);
        mFID = fid;
        mName = name;
        mPermissions = permissions;
        mMode = mode;
    }

    public void load(StyxInputStream input) throws IOException {
        mFID = input.readInt();
        mName = input.readUTF();
        mPermissions = input.readUInt32();
        mMode = ModeType.factory(input.readByte());
    }
    public void load(DualStateBuffer input) throws IOException {
        mFID = input.readUInt32();
        mName = input.readUTF();
        mPermissions = input.readUInt32();
        mMode = ModeType.factory(input.readUInt8());
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
        return super.getBinarySize() + 9
                + StyxMessage.getUTFSize(getName());
    }

    @Override
    public void writeToBuffer(StyxBufferOperations output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt(getFID());
        output.writeUTF(getName());
        output.writeUInt(getPermissions());
        output.writeUByte((short)getMode().getByte());  
    }
    @Override
    protected void internalWriteToStream(StyxOutputStream output)
            throws IOException 
            {
        output.writeUInt(getFID());
        output.writeUTF(getName());
        output.writeUInt(getPermissions());
        output.writeByte(getMode().getByte());		
            }

    @Override
    protected String internalToString() {
        return String.format("FID: %d\nName: %s\nPermissions: %d\nMode: %s", 
                getFID(), getName(), getPermissions(), getMode().toString());
    }

    @Override
    protected MessageType getNeeded() {
        return MessageType.Rcreate;
    }

}
