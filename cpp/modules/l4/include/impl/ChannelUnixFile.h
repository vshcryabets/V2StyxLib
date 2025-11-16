#pragma once

#include "Channel.h"

namespace styxlib
{
    using FileDescriptor = int;

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
        FileDescriptorPair fds{-1, -1};
        PacketHeaderSize packetSizeHeader;
    public:
        ChannelUnixFile(const PacketHeaderSize header)
            : ChannelTx(), ChannelRx(nullptr), packetSizeHeader(header)
        {
        }
        ~ChannelUnixFile() override;
        SizeResult sendBuffer(const StyxBuffer buffer, Size size) override;
        const ChannelUnixFile::FileDescriptorPair &getFileDescriptors() const;
    };

} // namespace styxlib