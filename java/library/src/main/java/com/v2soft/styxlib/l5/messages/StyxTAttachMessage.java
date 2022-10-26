package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.serialization.IStyxDataReader;
import com.v2soft.styxlib.l5.serialization.BufferWritter;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxTAttachMessage extends StyxTMessageFID {
    private long mAuthFID;
    private String mUserName;
    private String mMountPoint;

    public StyxTAttachMessage(long fid, long afid, String username, String mountpoint) {
        super(MessageType.Tattach, MessageType.Rattach, fid);
        mAuthFID = afid;
        mUserName = username;
        mMountPoint = mountpoint;
    }

    @Override
    public void load(IStyxDataReader input)
            throws IOException {
        super.load(input);
        setAuthFID(input.readUInt32());
        setUserName(input.readUTFString());
        setMountPoint(input.readUTFString());
    }

    public long getAuthFID() {
        return mAuthFID;
    }

    public void setAuthFID(long afid) {
        mAuthFID = afid;
    }

    public String getUserName() {
        if (mUserName == null) {
            return "";
        }
        return mUserName;
    }

    public void setUserName(String user_name) {
        mUserName = user_name;
    }

    public String getMountPoint() {
        if (mMountPoint == null) {
            return "";
        }
        return mMountPoint;
    }

    public void setMountPoint(String mount_point) {
        mMountPoint = mount_point;
    }

    @Override
    public int getBinarySize() {
        int res = super.getBinarySize() + 4
                + UTF.getUTFSize(getUserName())
                + UTF.getUTFSize(getMountPoint());
        return res;
    }

    @Override
    public String toString() {
        return String.format("%s\nAuthFID: %d\nUserName: %s\nMountPoint: %s",
                super.toString(),
                getAuthFID(),
                getUserName(),
                getMountPoint());
    }
}