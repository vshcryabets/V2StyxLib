package com.v2soft.styxlib.l5.serialization;

import com.v2soft.styxlib.l5.messages.base.StyxMessage;

import java.io.IOException;

public interface MessageSerializer {
    void serialize(StyxMessage message, BufferWritter output) throws IOException;
}
