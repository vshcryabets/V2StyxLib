package com.v2soft.styxlib.l5.serialization.impl;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.structs.StyxStat;

import java.io.IOException;

public class StyxDeserializerImpl implements IDataDeserializer {
    @Override
    public StyxMessage deserializeMessage(IBufferReader input) throws IOException {
        return null;
    }

    @Override
    public StyxStat deserializeState(IBufferReader input) throws IOException {
        return null;
    }
}
