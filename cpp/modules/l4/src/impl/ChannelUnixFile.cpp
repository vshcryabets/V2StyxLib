#include "impl/ChannelUnixFile.h"

#include <unistd.h>
#include <iostream>
#include <cstring>
#include <stdexcept>

namespace styxlib
{
    ChannelUnixFile::ChannelUnixFile(const ChannelUnixFile::Configuration &config)
        : ChannelTx(),
          ChannelRx(),
          fds(InvalidFileDescriptor, InvalidFileDescriptor),
          config(config)
    {
        if (setDeserializer(config.deserializer) != ErrorCode::Success) {
            throw std::invalid_argument("Deserializer cannot be null");
        }
        readBufferData.buffer.resize(config.iounit);
        readBufferData.currentSize = 0;
        readBufferData.isDirty = false;
    }

    ChannelUnixFile::~ChannelUnixFile()
    {
        closeDescriptors();
    }

    SizeResult ChannelUnixFile::sendBuffer(
        ClientId clientId, 
        const StyxBuffer buffer, 
        Size size)
    {
        if (fds.writeFd == InvalidFileDescriptor)
        {
            return Unexpected(ErrorCode::NotConnected);
        }
        // Send the buffer over the file descriptor
        uint8_t packetSizeBuffer[4] = {0};
        SizeResult headerSize = setPacketSize(
            config.packetSizeHeader, 
            packetSizeBuffer, 
            sizeof(packetSizeBuffer),
            size);
        if (!headerSize.has_value()) {
            return Unexpected(headerSize.error());
        }
        ssize_t result = ::write(fds.writeFd, packetSizeBuffer, headerSize.value());
        if (result < 0)
        {
            std::cerr << "Error writing packet size to fd " << fds.writeFd << ": " << strerror(errno) << std::endl;
            return Unexpected(ErrorCode::NotConnected);
        }
        result = ::write(fds.writeFd, buffer, size);
        if (result < 0)
        {
            return Unexpected(ErrorCode::NotConnected);
        }
        return static_cast<Size>(result);
    }

    void ChannelUnixFile::readBufferBlocking()
    {
        // read size header first
        uint8_t buffer[4];
        uint32_t packetSize = 0;
        ssize_t packetSizeReadResult = 0;
        switch (config.packetSizeHeader)
        {
        case PacketHeaderSize::Size1Byte:
            packetSizeReadResult = ::read(fds.readFd, buffer, 1);
            packetSize = buffer[0];
            break;
        case PacketHeaderSize::Size2Bytes:
            packetSizeReadResult = ::read(fds.readFd, buffer, 2);
            packetSize = buffer[1] | (buffer[0] << 8);
            break;
        case PacketHeaderSize::Size4Bytes:
            packetSizeReadResult = ::read(fds.readFd, buffer, 4);
            packetSize = buffer[3] |
                         (buffer[2] << 8) |
                         (buffer[1] << 16) |
                         (buffer[0] << 24);
            break;
        }
        if (packetSizeReadResult <= 0)
        {
            std::cerr << "Error reading packet size from fd " << fds.readFd << ": " << strerror(errno) << std::endl;
            return;
        }
        Size totalBytesRead = 0;
        while (totalBytesRead < packetSize)
        {
            ssize_t bytesRead = ::read(fds.readFd, readBufferData.buffer.data() + totalBytesRead, packetSize - totalBytesRead);
            if (bytesRead <= 0)
            {
                std::cerr << "Error reading packet data from fd " << fds.readFd << ": " << strerror(errno) << std::endl;
                return;
            }
            totalBytesRead += bytesRead;
        }
        readBufferData.currentSize = totalBytesRead;
        readBufferData.isDirty = true;
        // Notify the deserializer
        if (config.deserializer)
        {
            config.deserializer->handleBuffer(
                InvalidClientId,
                readBufferData.buffer.data(),
                readBufferData.currentSize);
        }
    }

    void ChannelUnixFile::closeDescriptors() {
        if (fds.readFd != InvalidFileDescriptor) {
            ::close(fds.readFd);
        }
        if (fds.writeFd != InvalidFileDescriptor) {
            ::close(fds.writeFd);
        }
        fds.readFd = InvalidFileDescriptor;
        fds.writeFd = InvalidFileDescriptor;
    }
} // namespace styxlib