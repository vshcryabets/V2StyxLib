#include "impl/ChannelUnixFile.h"

#include <unistd.h>

namespace styxlib
{
    ChannelUnixFile::ChannelUnixFile(const PacketHeaderSize header, DeserializerL4Ptr deserializer)
        : ChannelTx(),
          ChannelRx(deserializer),
          fds(InvalidFileDescriptor, InvalidFileDescriptor),
          packetSizeHeader(header)
    {
    }

    ChannelUnixFile::~ChannelUnixFile()
    {
        if (fds.readFd != InvalidFileDescriptor)
        {
            ::close(fds.readFd);
            fds.readFd = InvalidFileDescriptor;
        }
        if (fds.writeFd != InvalidFileDescriptor)
        {
            ::close(fds.writeFd);
            fds.writeFd = InvalidFileDescriptor;
        }
    }

    SizeResult ChannelUnixFile::sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size)
    {
        if (fds.writeFd != -1)
        {
            // Send the buffer over the file descriptor
            uint8_t packetSizeBuffer[4] = {0};
            switch (packetSizeHeader)
            {
            case PacketHeaderSize::Size1Byte:
                if (size > 255)
                {
                    return std::unexpected(ErrorCode::PacketTooLarge);
                }
                packetSizeBuffer[0] = static_cast<uint8_t>(size);
                break;
            case PacketHeaderSize::Size2Bytes:
                if (size > 65535)
                {
                    return std::unexpected(ErrorCode::PacketTooLarge);
                }
                packetSizeBuffer[0] = size & 0xFF;
                packetSizeBuffer[1] = (size >> 8) & 0xFF;
                break;

            case PacketHeaderSize::Size4Bytes:
                uint32_t networkSize32 = static_cast<uint32_t>(htonl(size));
                std::memcpy(packetSizeBuffer, &networkSize32, 4);
                break;
            }

            ::write(fds.writeFd, packetSizeBuffer, static_cast<uint8_t>(packetSizeHeader));
            return ::write(fds.writeFd, buffer, size);
        }
        else
        {
            return std::unexpected(ErrorCode::NotConnected);
        }
    }

    const ChannelUnixFile::FileDescriptorPair &ChannelUnixFile::getFileDescriptors() const
    {
        return fds;
    }
} // namespace styxlib