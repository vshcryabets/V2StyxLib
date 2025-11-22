#pragma once
#include <memory>
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
        ChannelRx(DeserializerL4Ptr deserializer) : deserializer(deserializer)
        {
            if (deserializer == nullptr)
            {
                throw std::invalid_argument("Deserializer cannot be null");
            }
        }
        virtual ~ChannelRx() = default;
    };
}