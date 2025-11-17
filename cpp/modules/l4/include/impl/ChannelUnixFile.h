#pragma once

#include "Channel.h"

namespace styxlib
{
    using FileDescriptor = int;
    constexpr FileDescriptor InvalidFileDescriptor = -1;

    class ChannelUnixFile: public ChannelTx, public ChannelRx
    {
    public:
        class FileDescriptorPair {
        public:
            FileDescriptor readFd;
            FileDescriptor writeFd;
            FileDescriptorPair(FileDescriptor readFd, FileDescriptor writeFd)
                : readFd(readFd), writeFd(writeFd) {}
        };
    protected:
        FileDescriptorPair fds;
        PacketHeaderSize packetSizeHeader;
    public:
        ChannelUnixFile(const PacketHeaderSize header, DeserializerL4Ptr deserializer);
        ~ChannelUnixFile() override;
        SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) override;
        const ChannelUnixFile::FileDescriptorPair &getFileDescriptors() const;
    };

} // namespace styxlib