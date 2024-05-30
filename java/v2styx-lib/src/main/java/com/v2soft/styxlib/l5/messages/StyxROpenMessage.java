package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;

public class StyxROpenMessage extends StyxRSingleQIDMessage {
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
