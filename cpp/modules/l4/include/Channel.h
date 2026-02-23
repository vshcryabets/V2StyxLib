#pragma once

#include <expected>

#include "data.h"
#include "SerializerL4.h"

namespace styxlib
{
    class ChannelRx
    {
    protected:
        DeserializerL4Ptr deserializer;

    public:
        ChannelRx();
        ErrorCode setDeserializer(DeserializerL4Ptr deserializer);
        virtual ~ChannelRx() = default;
    };
}