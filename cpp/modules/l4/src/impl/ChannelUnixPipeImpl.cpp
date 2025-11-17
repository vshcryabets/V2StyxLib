#include "impl/ChannelUnixPipeImpl.h"

#include <vector>
#include <iostream>
#include <unistd.h>
#include <poll.h>

namespace styxlib
{
    ChannelUnixPipeImpl::ChannelUnixPipeImpl(const PacketHeaderSize header, DeserializerL4Ptr deserializer)
        : ChannelUnixFile(header, deserializer)
    {
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
                    stopRequested.store(true);
                    this->startPromise = nullptr;
                    if (this->serverThread.joinable())
                    {
                        this->serverThread.join();
                    }
                }
            });
    }

    bool ChannelUnixPipeImpl::isStarted() const
    {
        return running.load();
    }

    void ChannelUnixPipeImpl::workThreadFunction()
    {
        running.store(true);
        // Create pipes
        int pipeFds[2];
        if (pipe(pipeFds) == -1)
        {
            startPromise->set_value(std::unexpected(ErrorCode::CantCreateSocket));
            return;
        }
        fds.readFd = pipeFds[0];
        fds.writeFd = pipeFds[1];
        startPromise->set_value(fds);
        std::vector<pollfd> pollFds;
        pollFds.push_back({fds.readFd, POLLIN, 0});

        while (!stopRequested.load())
        {
            int num_events = poll(pollFds.data(), pollFds.size(), 100); // 100 ms timeout
            if (stopRequested.load())
                break;
            if (num_events < 0)
            {
                std::cerr << "Poll error: " << strerror(errno) << std::endl;
                break;
            } else if (num_events == 0) {
                // Timeout, no events
                continue;
            } else {
                // handlePollEvents(serverSocket, num_events);
                if (stopRequested.load())
                    break;
                // processBuffers();
                // cleanupClosedSockets();
            }
        }
        ::close(fds.readFd);
        ::close(fds.writeFd);
        fds.readFd = InvalidFileDescriptor;
        fds.writeFd = InvalidFileDescriptor;
        running.store(false);
        stopRequested.store(false);
    }

} // namespace styxlib