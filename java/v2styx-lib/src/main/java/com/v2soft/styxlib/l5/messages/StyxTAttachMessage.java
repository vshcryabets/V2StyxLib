package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class StyxTAttachMessage extends StyxTMessageFID {
    public final long authFID;
    public final String userName;
    public final String mountPoint;

    public StyxTAttachMessage(long fid, long afid, String username, String mountpoint) {
        super(MessageType.Tattach, MessageType.Rattach, fid);
        authFID = afid;
        userName = username;
        mountPoint = mountpoint;
    }

    @Override
    public String toString() {
        return String.format("%s\nAuthFID: %d\nUserName: %s\nMountPoint: %s",
                super.toString(),
                authFID,
                userName,
                mountPoint);
    }
}
