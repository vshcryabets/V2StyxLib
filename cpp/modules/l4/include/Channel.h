#pragma once
#include <memory>

#include "data.h"
#include "SerializerL4.h"

namespace styxlib
{
    class ChannelRx
    {
    protected:
        DeserializerL4Ptr deserializer;
    public:
        ChannelRx(const std::string &tag, DeserializerL4Ptr deserializer) : deserializer(deserializer) {
            if (deserializer == nullptr) {
                throw std::invalid_argument("Deserializer cannot be null");
            }
        }
        virtual ~ChannelRx() = default;
    };

    class ChannelTx
    {
    public:
        virtual ~ChannelTx() = default;
        virtual Size sendBuffer(const StyxBuffer buffer, Size size) = 0;
    };

    class ChannelTxOneToMany {
    public:
        virtual ~ChannelTxOneToMany() = default;
        virtual Size sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) = 0;
    };
}