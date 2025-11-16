#pragma once

#include <expected>

#include "ChannelUnixFile.h"

namespace styxlib
{
    class ChannelUnixPipeImpl : public ChannelUnixFile
    {        
    public:
        ChannelUnixPipeImpl(const PacketHeaderSize header);
        std::future<std::expected<ChannelUnixFile::FileDescriptorPair, ErrorCode>> start();
        std::future<ErrorCode> connect(const FileDescriptorPair& fds);
        bool isStarted() const;
        bool isConnected() const;
        std::future<void> stop();
    };
} // namespace styxlib