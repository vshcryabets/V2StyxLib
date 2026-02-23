#pragma once

#include <vector>
#include <cstdint>

#include "data.h"
#include "ChannelRx.h"
#include "ChannelTx.h"

namespace styxlib
{
    using FileDescriptor = int;
    constexpr FileDescriptor InvalidFileDescriptor = -1;

    struct ReadBuffer
    {
        std::vector<uint8_t> buffer;
        Size currentSize{0};
        bool isDirty{false};
    };

    class ChannelUnixFile : public ChannelTx, public ChannelRx
    {
    public:
        struct Configuration
        {
            PacketHeaderSize packetSizeHeader{PacketHeaderSize::Size2Bytes};
            uint16_t iounit{8192};
            DeserializerL4Ptr deserializer{nullptr};
            Configuration(
                PacketHeaderSize packetSizeHeader,
                uint16_t iounit,
                DeserializerL4Ptr deserializer)
                : packetSizeHeader(packetSizeHeader),
                  iounit(iounit),
                  deserializer(deserializer) {}
        };
        class FileDescriptorPair
        {
        public:
            FileDescriptor readFd;
            FileDescriptor writeFd;
            FileDescriptorPair(FileDescriptor readFd, FileDescriptor writeFd)
                : readFd(readFd), writeFd(writeFd) {}
        };

    protected:
        FileDescriptorPair fds;
        Configuration config;
        ReadBuffer readBufferData;

        virtual void closeDescriptors();
    public:
        ChannelUnixFile(const Configuration &config);
        ~ChannelUnixFile() override;
        SizeResult sendBuffer(
            ClientId clientId,
            const StyxBuffer buffer,
            Size size) override;
        void readBufferBlocking();
    };
} // namespace styxlib