package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.Config;
import com.v2soft.styxlib.l5.dev.Operations;
import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxRReadMessage extends BaseMessage {
    public final byte[] data;
    public final int dataLength;

    protected StyxRReadMessage(int tag, byte[] data, int length) {
        super(MessageType.Rread, tag, null);
        if (length < 0) {
            throw new IllegalArgumentException("length is negative " + length);
        }
        this.data = data;
        dataLength = length;
    }

    @Override
    public String toString() {
        if ( Config.LOG_DATA_FIELDS ) {
            return String.format("%s\nData Length:%d\nData: %s",
                    super.toString(),
                    dataLength,
                    Operations.toString(data));
        } else {
            return String.format("%s\nData Length:%d",
                    super.toString(),
                    dataLength);
        }
    }
}
