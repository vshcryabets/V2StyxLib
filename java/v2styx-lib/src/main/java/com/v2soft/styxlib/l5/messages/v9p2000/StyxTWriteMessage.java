package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxTWriteMessage extends StyxTMessage {
    public final long offset;
    public final byte[] data;
    public final int dataOffset;
    public final int dataLength;

    protected StyxTWriteMessage(long fid,
                             long offset,
                             byte [] data,
                             int dataOffset,
                             int dataLength) {
        super(MessageType.Twrite, null, fid);
        this.offset = offset;
        this.data = data;
        this.dataLength = dataLength;
        this.dataOffset = dataOffset;
    }
}
