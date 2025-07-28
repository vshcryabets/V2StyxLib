package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class StyxTWriteMessage extends StyxTMessageFID {
    public final long offset;
    public final byte[] data;
    public final int dataOffset;
    public final int dataLength;

    protected StyxTWriteMessage(long fid,
                             long offset,
                             byte [] data,
                             int dataOffset,
                             int dataLength) {
        super(MessageType.Twrite, MessageType.Rwrite, fid);
        this.offset = offset;
        this.data = data;
        this.dataLength = dataLength;
        this.dataOffset = dataOffset;
    }
}
