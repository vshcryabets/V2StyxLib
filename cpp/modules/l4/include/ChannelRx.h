#pragma once

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

    /**
     * Reads the first X bytes of buffer and interprets them as a packet size.
     * @param headerSize The size of the packet header (1, 2, or 4 bytes).
     * @param buffer The buffer to read from.
     * @param bufferSize The number of valid bytes available in the buffer.
     * @return The decoded packet payload size, or an ErrorCode if the buffer
     *         is too small to hold the header.
     */
    SizeResult getPacketSize(
        const PacketHeaderSize &headerSize,
        const uint8_t *buffer,
        Size bufferSize);
}