package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l5.structs.StyxStat;

public interface IDataDeserializer {
    StyxMessage deserializeMessage(IBufferReader input, int io_unit) throws StyxException;
    StyxStat deserializeStat(IBufferReader input) throws StyxException;
    StyxQID deserializeQid(IBufferReader input) throws StyxException;
}
