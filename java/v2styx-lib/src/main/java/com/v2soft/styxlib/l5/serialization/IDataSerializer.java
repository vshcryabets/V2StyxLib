package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.QID;
import com.v2soft.styxlib.l5.structs.StyxStat;

public interface IDataSerializer {
    int BASE_BINARY_SIZE = 7;
    int getMessageSize(StyxMessage message);
    void serialize(StyxMessage message, IBufferWriter output) throws StyxException;
    void serializeStat(StyxStat stat, IBufferWriter output) throws StyxException;
    int getStatSerializedSize(StyxStat stat);
    int getQidSize();
    void serializeQid(QID qid, IBufferWriter output) throws StyxException;
}
