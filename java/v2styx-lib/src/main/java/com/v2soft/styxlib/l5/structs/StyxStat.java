package com.v2soft.styxlib.l5.structs;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;
import java.util.Date;

public class StyxStat {
    public static final StyxStat EMPTY = new StyxStat(
            (short)0,
            0,
            StyxQID.EMPTY,
            0,
            new Date(),
            new Date(),
            0,
            "",
            "",
            "",
            "");

    private int mType; //for kernel use
    private long mDev; //for kernel use
    private StyxQID mQID;
    private long mMode; // permissions and flags
    private Date mAccessTime; // last access time
    private Date mModificationTime; // last modification time
    private long mLength; //length of file in bytes
    private String mName; // file name; must be / if the file is the root directory of the server
    private String mUserName; //owner name
    private String mGroupName; //group name
    private String mModificationUser; //name of the user who last modified the file

    public static Date IntToDate(long date)
    {
        return new Date(date * 1000L);
    }

    public StyxStat(short type,
                    int dev,
                    StyxQID qid,
                    int mode,
                    Date accessTime,
                    Date modificationTime,
                    long length,
                    String name,
                    String userName,
                    String groupName,
                    String modificationUser) {
        mType = type;
        mDev = dev;
        mQID = qid;
        mMode = mode;
        mAccessTime = accessTime;
        mModificationTime = modificationTime;
        mLength = length;
        mName = name;
        mUserName = userName;
        mGroupName = groupName;
        mModificationUser = modificationUser;
    }

    public StyxStat(IBufferReader input) throws StyxException {
        // TODO move to factory
        int size = input.readUInt16(); // skip size bytes
        // TODO check size
        mType = input.readUInt16();
        mDev = input.readUInt32();
        mQID = new StyxQID(input);
        mMode = input.readUInt32();
        mAccessTime = IntToDate(input.readUInt32());
        mModificationTime = IntToDate(input.readUInt32());
        mLength = input.readUInt64();
        mName = input.readUTFString();
        mUserName = input.readUTFString();
        mGroupName = input.readUTFString();
        mModificationUser = input.readUTFString();
    }

    public int getSize()
    {
        return 28 + StyxQID.CONTENT_SIZE
                + UTF.getUTFSize(mName)
                + UTF.getUTFSize(mUserName)
                + UTF.getUTFSize(mGroupName)
                + UTF.getUTFSize(mModificationUser);
    }

    public int getType()
    {
        return mType;
    }

    public long getDev()
    {
        return mDev;
    }

    public StyxQID getQID()
    {
        if (mQID == null)
            return StyxQID.EMPTY;
        return mQID;
    }

    public long getMode()
    {
        return mMode;
    }

    public void setMode(long mode)
    {
        mMode = mode;
    }

    public Date getAccessTime()
    {
        if (mAccessTime == null)
            return IntToDate(0);
        return mAccessTime;
    }

    public Date getModificationTime()
    {
        if (mModificationTime == null)
            return IntToDate(0); // TODO ???
        return mModificationTime;
    }

    public long getLength()
    {
        return mLength;
    }

    public void setLength(long length)
    {
        mLength = length;
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

    public String getUserName()
    {
        if (mUserName == null)
            return "";
        return mUserName;
    }

    public String getGroupName()
    {
        if (mGroupName == null)
            return "";
        return mGroupName;
    }

    public String getModificationUser()
    {
        if (mModificationUser == null)
            return "";
        return mModificationUser;
    }

    @Override
    public String toString() {
        return String.format("(Type: %d; Dev: %d; QID: %s; Mode: %d;"
                + " AccessTime: %s; ModificationTime: %s; Length: %d;"
                + " Name: %s; UserName: %s; GroupName: %s;"
                + " ModificationUser: %s)",
                getType(), getDev(), getQID().toString(), getMode(),
                getAccessTime().toString(), getModificationTime().toString(),
                getLength(), getName(), getUserName(), getGroupName(),
                getModificationUser());
    }
}
