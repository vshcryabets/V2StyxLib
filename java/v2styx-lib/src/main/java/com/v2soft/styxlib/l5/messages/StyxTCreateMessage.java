package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class StyxTCreateMessage extends StyxTMessageFID {
    public final String name;
    public final long permissions;
    public final int mode;

    public StyxTCreateMessage(long fid, String name, long permissions, int mode) {
        super(MessageType.Tcreate, fid);
        this.name = name;
        this.permissions = permissions;
        this.mode = mode;
    }

    @Override
    public String toString() {
        return String.format("%s\nName: %s\nPermissions: %d\nMode: %d",
                super.toString(),
                name,
                permissions,
                mode);
    }
}
