package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;

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
    public void load(IBufferReader input)
            throws IOException {
        super.load(input);
        mAuthFID = input.readUInt32();
        mUserName = input.readUTFString();
        mMountPoint =  input.readUTFString();
    }

    public long getAuthFID() {
        return mAuthFID;
    }

    public String getUserName() {
        if (mUserName == null) {
            return "";
        }
        return mUserName;
    }

    public String getMountPoint() {
        if (mMountPoint == null) {
            return "";
        }
        return mMountPoint;
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
