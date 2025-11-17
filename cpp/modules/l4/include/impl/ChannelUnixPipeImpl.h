#pragma once

#include <expected>
#include <future>
#include <atomic>
#include <memory>
#include <thread>

#include "ChannelUnixFile.h"

namespace styxlib
{
    class ChannelUnixPipeImpl : public ChannelUnixFile
    {
    public:
        using StartResult = std::expected<ChannelUnixFile::FileDescriptorPair, ErrorCode>;
    private:
        std::thread serverThread;
        std::atomic<bool> running{false};
        std::atomic<bool> stopRequested{false};
        std::unique_ptr<std::promise<StartResult>> startPromise;
    private:
        void workThreadFunction();
    public:
        ChannelUnixPipeImpl(const PacketHeaderSize header, DeserializerL4Ptr deserializer);
        std::future<StartResult> start();
        std::future<ErrorCode> connect(const FileDescriptorPair& fds);
        bool isStarted() const;
        bool isConnected() const;
        std::future<void> stop();
    };
} // namespace styxlib