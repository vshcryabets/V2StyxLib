package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.IOException;

public interface IDataSerializer {
    public static final int BASE_BINARY_SIZE = 7;

    void serialize(StyxMessage message, IBufferWritter output) throws StyxException;

    void serializeStat(StyxStat stat, IBufferWritter output) throws StyxException;
}
