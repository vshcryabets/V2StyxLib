package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;

public class StyxROpenMessage extends BaseMessage {
    public final long ioUnit;

    public StyxROpenMessage(int tag,
                            StyxQID qid,
                            long iounit,
                            boolean create) {
        super(( create ? MessageType.Rcreate : MessageType.Ropen ), tag, qid);
        ioUnit = iounit;
    }

    @Override
    public String toString() {
        return String.format("%s\nIOUnit: %d",
                super.toString(),
                ioUnit);
    }
}
