#pragma once

#include "data.h"

namespace styxlib
{
    class ChannelTx
    {
    public:
        virtual ~ChannelTx() = default;
        virtual SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) = 0;
    };

    /**
     * Sets the packet size in the provided buffer according to the specified header size.
     * @param headerSize The size of the packet header (1, 2, or 4 bytes).
     * @param buffer The buffer where the packet size will be set.
     * @param bufferSize The size of the buffer.
     * @param packetSize The size of the packet to set.
     * @return The number of bytes used for the header, or an ErrorCode if an error occurs.
     */
    SizeResult setPacketSize(
        const PacketHeaderSize &headerSize,
        uint8_t *buffer,
        Size bufferSize,
        Size packetSize);
}

