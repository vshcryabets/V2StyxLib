#include "ChannelTx.h"

namespace styxlib
{
    SizeResult setPacketSize(const PacketHeaderSize &headerSize,
                          uint8_t* buffer,
                          Size bufferSize,
                          Size packetSize) {
        if (bufferSize < 4) {
            return -1;
        }
        switch (headerSize)
        {
        case PacketHeaderSize::Size1Byte:
            if (packetSize > 0xFF) {
                return -1;
            }
            buffer[0] = static_cast<uint8_t>(packetSize);
            break;
        case PacketHeaderSize::Size2Bytes:
            if (packetSize > 0xFFFF) {
                return -1;
            }
            buffer[1] = packetSize & 0xFF;
            buffer[0] = (packetSize >> 8) & 0xFF;
            break;
        case PacketHeaderSize::Size4Bytes:
            if (packetSize > 0xFFFFFFFF) {
                return -1;
            }
            buffer[3] = packetSize & 0xFF;
            buffer[2] = (packetSize >> 8) & 0xFF;
            buffer[1] = (packetSize >> 16) & 0xFF;
            buffer[0] = (packetSize >> 24) & 0xFF;
            break;
        }
        return static_cast<uint8_t>(headerSize);
    }
} // namespace styxlib