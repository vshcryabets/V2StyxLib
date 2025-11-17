#pragma once
#include <memory>
#include <expected>

#include "data.h"
#include "SerializerL4.h"

namespace styxlib
{
    enum class PacketHeaderSize : uint8_t
    {
        Size1Byte = 1,
        Size2Bytes = 2,
        Size4Bytes = 4
    };

    inline uint8_t to_uint8_t(const PacketHeaderSize &headerSize)
    {
        return static_cast<uint8_t>(headerSize);
    }

    /**
     * Sets the packet size in the provided buffer according to the specified header size.
     * @param headerSize The size of the packet header (1, 2, or 4 bytes).
     * @param buffer The buffer where the packet size will be set.
     * @param bufferSize The size of the buffer.
     * @param packetSize The size of the packet to set.
     * @return The number of bytes used for the header, or an ErrorCode if an error occurs.
     */
    std::expected<uint8_t, ErrorCode> setPacketSize(
        const PacketHeaderSize &headerSize,
        uint8_t *buffer,
        Size bufferSize,
        Size packetSize);

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

    class ChannelTx
    {
    public:
        virtual ~ChannelTx() = default;
        virtual SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) = 0;
    };
}