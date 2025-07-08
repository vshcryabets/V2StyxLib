package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.l5.dev.Operations;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class StyxTWriteMessage extends StyxTMessageFID {
    public final long offset;
    public final byte[] data;
    public final int dataOffset;
    public final int dataLength;

    public StyxTWriteMessage(long fid,
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

    @Override
    public String toString() {
        if ( Config.LOG_DATA_FIELDS ) {
            return String.format("%s\nOffset: %d\nData: %s",
                    super.toString(), offset, Operations.toString(data));
        } else {
            return String.format("%s\nOffset: %d",
                    super.toString(), offset);
        }
    }
}
