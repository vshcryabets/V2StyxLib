package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxTCreateMessage extends StyxTMessage {
    public final String name;
    public final long permissions;
    public final int mode;

    protected StyxTCreateMessage(long fid, String name, long permissions, int mode) {
        super(MessageType.Tcreate, null, fid);
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
