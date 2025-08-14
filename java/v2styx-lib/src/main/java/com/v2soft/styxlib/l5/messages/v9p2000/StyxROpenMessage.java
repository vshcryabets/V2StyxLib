package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.StyxQID;

public class StyxROpenMessage extends BaseMessage {
    public final long ioUnit;

    protected StyxROpenMessage(int tag,
                            StyxQID qid,
                            long iounit,
                            boolean create) {
        super(( create ? MessageType.Rcreate : MessageType.Ropen ), tag, qid, 0);
        ioUnit = iounit;
    }
}
