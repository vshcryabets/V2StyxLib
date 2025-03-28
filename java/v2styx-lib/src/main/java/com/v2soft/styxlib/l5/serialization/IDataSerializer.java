package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

public interface IDataSerializer {
    int BASE_BINARY_SIZE = 7;

    void serialize(StyxMessage message, IBufferWritter output) throws StyxException;
    void serializeStat(StyxStat stat, IBufferWritter output) throws StyxException;
    int getStatSerializedSize(StyxStat stat);
    int getQidSize();
    void serializeQid(StyxQID qid, IBufferWritter output) throws StyxException;
}
