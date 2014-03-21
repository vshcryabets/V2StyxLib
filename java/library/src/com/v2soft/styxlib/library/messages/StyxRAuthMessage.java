package com.v2soft.styxlib.library.messages;

import com.v2soft.styxlib.library.messages.base.StyxRSingleQIDMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;

public class StyxRAuthMessage extends StyxRSingleQIDMessage {
    public StyxRAuthMessage(int tag, StyxQID qid) {
        super(MessageType.Rauth, tag, qid);
    }
}