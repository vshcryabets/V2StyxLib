package com.v2soft.styxlib.l5.messages.base;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.structs.StyxQID;

import java.io.IOException;

public class StyxRSingleQIDMessage extends StyxMessage {
    protected final StyxQID mQID;

    public StyxRSingleQIDMessage(MessageType type, int tag, StyxQID qid) {
        super(type, tag);
        mQID = qid;
    }

    /**
     * @return QID structure
     */
    public StyxQID getQID() {
        return mQID;
    }

    @Override
    public String toString() {
        return String.format("%s\tQID: %s", super.toString(), getQID().toString());
    }
}
