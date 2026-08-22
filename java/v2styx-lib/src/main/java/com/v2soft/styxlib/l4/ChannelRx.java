package com.v2soft.styxlib.l4;

public abstract class ChannelRx {
    private DeserializerL4 deserializer;

    abstract void onReceive(StyxBuffer buffer);

    ErrorCode setDeserializer(DeserializerL4 deserializer) {
        if (deserializer == null) {
            return ErrorCode.NullptrArgument;
        }
        this.deserializer = deserializer;
        return ErrorCode.Success;
    }

}
