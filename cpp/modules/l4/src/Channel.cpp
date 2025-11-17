#include "Channel.h"

namespace styxlib
{
    uint8_t setPacketSize(const PacketHeaderSize &headerSize,
                          uint8_t* buffer,
                          Size size) {
        if (size < 4) {
            throw std::invalid_argument("Size must be at least 4 to fit the header");
        }
        switch (headerSize)
        {
        case PacketHeaderSize::Size1Byte:
            buffer[0] = static_cast<uint8_t>(size);
            break;
        case PacketHeaderSize::Size2Bytes:
            buffer[1] = size & 0xFF;
            buffer[0] = (size >> 8) & 0xFF;
            break;
        case PacketHeaderSize::Size4Bytes:
            buffer[3] = size & 0xFF;
            buffer[2] = (size >> 8) & 0xFF;
            buffer[1] = (size >> 16) & 0xFF;
            buffer[0] = (size >> 24) & 0xFF;
            break;
        }
        return static_cast<uint8_t>(headerSize);
    }
} // namespace styxlib