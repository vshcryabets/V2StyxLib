#include "ChannelTx.h"

namespace styxlib
{
    SizeResult setPacketSize(const PacketHeaderSize &headerSize,
                          uint8_t* buffer,
                          Size bufferSize,
                          Size packetSize) {
        if (bufferSize < 4) {
            return styxlib::Unexpected(ErrorCode::BufferTooSmall);
        }
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wtype-limits"        
        switch (headerSize)
        {
        case PacketHeaderSize::Size1Byte:
            if constexpr (sizeof(packetSize) > 1) {
                if (packetSize > 0xFF) {
                    return styxlib::Unexpected(ErrorCode::PacketTooLarge);
                }
            }
            buffer[0] = static_cast<uint8_t>(packetSize);
            break;
        case PacketHeaderSize::Size2Bytes:
            if constexpr (sizeof(packetSize) > 2) {
                if (packetSize > 0xFFFF) {
                    return styxlib::Unexpected(ErrorCode::PacketTooLarge);
                }
            }
            buffer[1] = packetSize & 0xFF;
            buffer[0] = (packetSize >> 8) & 0xFF;
            break;
        case PacketHeaderSize::Size4Bytes:
            if constexpr (sizeof(packetSize) > 4) {
                if (packetSize > 0xFFFFFFFF) {
                    return styxlib::Unexpected(ErrorCode::PacketTooLarge);
                }
            }
            buffer[3] = packetSize & 0xFF;
            buffer[2] = (packetSize >> 8) & 0xFF;
            buffer[1] = (packetSize >> 16) & 0xFF;
            buffer[0] = (packetSize >> 24) & 0xFF;
            break;
        default:
            return styxlib::Unexpected(ErrorCode::InvalidHeaderSize);
        }
#pragma GCC diagnostic pop        
        return styxlib::SizeResult(static_cast<Size>(headerSize));
    }
} // namespace styxlib