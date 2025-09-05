package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.QID;

public class StyxTAttachMessage extends StyxTMessage {
    public final long authFID;
    public final String userName;
    public final String mountPoint;

    protected StyxTAttachMessage(long fid, long authFid, String username, String mountpoint) {
        super(MessageType.Tattach, QID.EMPTY, fid, 0, null);
        authFID = authFid;
        userName = username;
        mountPoint = mountpoint;
    }
}
