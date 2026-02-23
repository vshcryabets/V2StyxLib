#include "Channel.h"

namespace styxlib
{
    ChannelRx::ChannelRx() : deserializer(nullptr)
    {
    }

    ErrorCode ChannelRx::setDeserializer(DeserializerL4Ptr deserializer)
    {
        if (deserializer == nullptr)
        {
            return ErrorCode::NullptrArgument;
        }
        this->deserializer = deserializer;
        return ErrorCode::Success;
    }

    std::expected<Size, ErrorCode> getPacketSize(
        const PacketHeaderSize &headerSize,
        const uint8_t *buffer,
        Size bufferSize)
    {
        const uint8_t needed = static_cast<uint8_t>(headerSize);
        if (bufferSize < needed)
        {
            return std::unexpected(ErrorCode::BufferTooSmall);
        }
        switch (headerSize)
        {
        case PacketHeaderSize::Size1Byte:
            return static_cast<Size>(buffer[0]);
        case PacketHeaderSize::Size2Bytes:
            return static_cast<Size>(
                (static_cast<uint16_t>(buffer[0]) << 8) |
                 static_cast<uint16_t>(buffer[1]));
        case PacketHeaderSize::Size4Bytes:
            return static_cast<Size>(
                (static_cast<uint32_t>(buffer[0]) << 24) |
                (static_cast<uint32_t>(buffer[1]) << 16) |
                (static_cast<uint32_t>(buffer[2]) <<  8) |
                 static_cast<uint32_t>(buffer[3]));
        }
        return std::unexpected(ErrorCode::InvalidHeaderSize);
    }
} // namespace styxlib