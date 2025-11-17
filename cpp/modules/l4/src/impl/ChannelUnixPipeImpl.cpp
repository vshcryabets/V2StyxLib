#include "impl/ChannelUnixPipeImpl.h"
#include <unistd.h>

namespace styxlib
{
    ChannelUnixPipeImpl::ChannelUnixPipeImpl(const PacketHeaderSize header)
        : ChannelUnixFile(header)
    {
        // int pipeFds[2];
        // if (pipe(pipeFds) == -1)
        // {
        //     throw std::runtime_error("Failed to create pipe");
        // }
        // readFd = pipeFds[0];
        // writeFd = pipeFds[1];
    }

    std::future<ErrorCode> ChannelUnixPipeImpl::connect(
        const ChannelUnixFile::FileDescriptorPair &fds)
    {
        return std::async(
            std::launch::async,
            [this, fds]()
            {
                this->fds.readFd = fds.readFd;
                this->fds.writeFd = fds.writeFd;
                return ErrorCode::Success;
            });
    }

    std::future<ChannelUnixPipeImpl::StartResult> ChannelUnixPipeImpl::start()
    {
        startPromise =
            std::make_unique<
                std::promise<
                    std::expected<ChannelUnixFile::FileDescriptorPair, ErrorCode>>>();
        if (!isStarted())
        {
            stopRequested.store(false);
            serverThread = std::thread([this]()
                                       { this->workThreadFunction(); });
        }
        else
        {
            startPromise->set_value(std::unexpected(ErrorCode::AlreadyStarted));
        }
        return startPromise->get_future();
    }

    std::future<void> ChannelUnixPipeImpl::stop()
    {
        return std::async(
            std::launch::async,
            [this]()
            {
                if (isStarted())
                {
                    ::close(fds.readFd);
                    ::close(fds.writeFd);
                    fds.readFd = -1;
                    fds.writeFd = -1;
                }
            });
    }
} // namespace styxlib