package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.IOException;

public interface IDataSerializer {
    void serialize(StyxMessage message, IBufferWritter output) throws IOException;

    void serializeStat(StyxStat stat, IBufferWritter output) throws IOException;
}
