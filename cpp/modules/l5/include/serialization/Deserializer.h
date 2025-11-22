#pragma once
#include "messages/base/StyxMessage.h"
#include "Channel.h"
#include "SerializerL4.h"

namespace styxlib
{
    class Deserializer: public DeserializerL4
    {
    public:
        Deserializer() : DeserializerL4() {}
        virtual ~Deserializer() = default;
    };
}