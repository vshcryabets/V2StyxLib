#include "impl/ChannelUnixPipeImpl.h"

#include <vector>
#include <iostream>
#include <unistd.h>
#include <poll.h>

namespace styxlib
{
    ChannelUnixPipeImpl::ChannelUnixPipeImpl(const ChannelUnixFile::Configuration &config)
        : ChannelUnixFile(config),
            rxFds(InvalidFileDescriptor, InvalidFileDescriptor),
            txFds(InvalidFileDescriptor, InvalidFileDescriptor)
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
                isDescriptorOwned = false;
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
                } else {
                    // Just close descriptors if owned
                    closeDescriptors();
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
        rxFds.readFd = pipeFds[0];
        rxFds.writeFd = pipeFds[1];
        if (pipe(pipeFds) == -1)
        {
            startPromise->set_value(std::unexpected(ErrorCode::CantCreateSocket));
            return;
        }
        txFds.readFd = pipeFds[0];
        txFds.writeFd = pipeFds[1];
        fds.readFd = rxFds.readFd;
        fds.writeFd = txFds.writeFd;

        isDescriptorOwned = true;
        startPromise->set_value(ChannelUnixFile::FileDescriptorPair{
            txFds.readFd,
            rxFds.writeFd});
        std::vector<pollfd> pollFds;
        // let's listen on rx read fd
        pollFds.push_back({rxFds.readFd, POLLIN, 0});

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
                readBufferBlocking();
            }
        }
        closeDescriptors();
        running.store(false);
        stopRequested.store(false);
    }

    bool ChannelUnixPipeImpl::isConnected() const {
        return fds.readFd != InvalidFileDescriptor && fds.writeFd != InvalidFileDescriptor;
    }

    ChannelUnixFile::FileDescriptorPair ChannelUnixPipeImpl::getClientFileDescriptors() const {
        return ChannelUnixFile::FileDescriptorPair{
            txFds.readFd,
            rxFds.writeFd
        };
    }

    void ChannelUnixPipeImpl::closeDescriptors() {
        if (isDescriptorOwned) {
            if (rxFds.readFd != InvalidFileDescriptor) {
                ::close(rxFds.readFd);
            }
            if (rxFds.writeFd != InvalidFileDescriptor) {
                ::close(rxFds.writeFd);
            }
            if (txFds.readFd != InvalidFileDescriptor) {
                ::close(txFds.readFd);
            }
            if (txFds.writeFd != InvalidFileDescriptor) {
                ::close(txFds.writeFd);
            }
            isDescriptorOwned = false;
        }
        fds.readFd = InvalidFileDescriptor;
        fds.writeFd = InvalidFileDescriptor;
        rxFds.readFd = InvalidFileDescriptor;
        rxFds.writeFd = InvalidFileDescriptor;
        txFds.readFd = InvalidFileDescriptor;
        txFds.writeFd = InvalidFileDescriptor;
    }

} // namespace styxlib