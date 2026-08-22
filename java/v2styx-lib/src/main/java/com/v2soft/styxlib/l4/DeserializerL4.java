package com.v2soft.styxlib.l4;

public interface DeserializerL4 {
    void handleBuffer(ClientId clientId, StyxBuffer buffer);
}
