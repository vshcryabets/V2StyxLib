package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.IOException;

public interface IDataDeserializer {
    StyxMessage deserializeMessage(IBufferReader input) throws IOException;
    StyxStat deserializeState(IBufferReader input) throws IOException;
}
