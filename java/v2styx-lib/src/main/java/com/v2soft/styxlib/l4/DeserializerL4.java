package com.v2soft.styxlib.l4;

public interface DeserializerL4 {
    ErrorCode handleBuffer(ClientId clientId, StyxBuffer buffer);
}
