package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.IOException;

public interface IDataDeserializer {
    StyxMessage deserializeMessage(IBufferReader input, int io_unit) throws StyxException;
    StyxStat deserializeStat(IBufferReader input) throws IOException;
}
