package com.v2soft.styxlib.l5.dev;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.QID;
import com.v2soft.styxlib.l5.structs.StyxStat;

// String serialization for debug purposes
public interface StringSerializer {
    String serializeQid(QID qid);
    String serializeStat(StyxStat stat) throws StyxException;
    String serializeMessage(StyxMessage message);
}
