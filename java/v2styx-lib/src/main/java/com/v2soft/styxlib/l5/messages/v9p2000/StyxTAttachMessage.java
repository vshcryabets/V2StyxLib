package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxTAttachMessage extends StyxTMessage {
    public final long authFID;
    public final String userName;
    public final String mountPoint;

    protected StyxTAttachMessage(long fid, long afid, String username, String mountpoint) {
        super(MessageType.Tattach, null, fid);
        authFID = afid;
        userName = username;
        mountPoint = mountpoint;
    }
}
