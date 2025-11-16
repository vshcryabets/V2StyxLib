#pragma once
#include <memory>

#include "data.h"
#include "SerializerL4.h"

namespace styxlib
{
    enum class PacketHeaderSize : uint8_t {
        Size1Byte = 1,
        Size2Bytes = 2,
        Size4Bytes = 4
    };

    class ChannelRx
    {
    protected:
        DeserializerL4Ptr deserializer;
    public:
        ChannelRx(DeserializerL4Ptr deserializer) : deserializer(deserializer) {
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
        virtual SizeResult sendBuffer(const StyxBuffer buffer, Size size) = 0;
    };

    class ChannelTxOneToMany {
    public:
        virtual ~ChannelTxOneToMany() = default;
        virtual SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) = 0;
    };
}