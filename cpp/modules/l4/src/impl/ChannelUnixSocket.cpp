#include "impl/ChannelUnixSocket.h"

#include <sys/socket.h>
#include <unistd.h>
#include <cstring>
#include <vector>
#include <future>
#include <iostream>
#include <algorithm>
#include <ranges>

namespace styxlib
{
    ChannelUnixSocketTx::ChannelUnixSocketTx(
        PacketHeaderSize packetSizeHeader,
        std::optional<Socket> socket)
        : packetSizeHeader(packetSizeHeader), 
        socket(socket)
    {
    }

    SizeResult ChannelUnixSocketTx::sendBuffer(
        ClientId clientId,
        const StyxBuffer buffer,
        Size size)
    {
        if (!socket.has_value())
        {
            return Unexpected(ErrorCode::NotConnected);
        }

        uint8_t packetSizeBuffer[4] = {0};
        SizeResult headerSize = setPacketSize(
            packetSizeHeader,
            packetSizeBuffer,
            sizeof(packetSizeBuffer),
            size);
        if (!headerSize.has_value())
        {
            return Unexpected(headerSize.error());
        }

        // Combine header and payload into a single buffer so the send is
        // atomic. This is essential for UDP (each ::send() maps to one
        // datagram) and harmless for TCP.
        // TODO allocate IOUnit buffer at construction and reuse it here to avoid this copy.
        std::vector<uint8_t> combined(headerSize.value() + size);
        std::memcpy(combined.data(), packetSizeBuffer, headerSize.value());
        std::memcpy(combined.data() + headerSize.value(), buffer, size);
        Size bytesSent = static_cast<Size>(::send(socket.value(), combined.data(), combined.size(), 0));
        return bytesSent - headerSize.value();
    }

    ChannelUnixSocketClient::ChannelUnixSocketClient(const Configuration &config)
        : ChannelUnixSocketTx(config.packetSizeHeader, std::nullopt),
          ChannelRx(),
          configuration(config)
    {
        if (setDeserializer(config.deserializer) != ErrorCode::Success) {
            throw std::invalid_argument("Deserializer cannot be null");
        }
    }

    ChannelUnixSocketClient::~ChannelUnixSocketClient()
    {
        disconnect().get();
    }

    std::future<void> ChannelUnixSocketClient::disconnect()
    {
        return std::async(
            std::launch::async,
            [this]()
            {
                if (socket.has_value())
                {
                    ::close(socket.value());
                    socket = std::nullopt;
                }
            });
    }

    bool ChannelUnixSocketClient::isConnected() const
    {
        return socket.has_value();
    }

    // ── ChannelUnixSocketServer ───────────────────────────────────────────────

    ChannelUnixSocketServer::ChannelUnixSocketServer(const Configuration &config)
        : ChannelRx(), configuration(config)
    {
        if (setDeserializer(config.deserializer) != ErrorCode::Success) {
            throw std::invalid_argument("Deserializer cannot be null");
        }
        if (configuration.clientsRepo == nullptr)
        {
            throw std::invalid_argument("ClientsRepo must be provided in configuration");
        }
    }

    ChannelUnixSocketServer::~ChannelUnixSocketServer()
    {
        stop().get();
    }

    SizeResult ChannelUnixSocketServer::sendBuffer(
        ClientId clientId,
        const StyxBuffer buffer,
        Size size)
    {
        if (!isStarted())
        {
            return Unexpected(ErrorCode::NotConnected);
        }
        auto it = clientIdToChannelClient.find(clientId);
        if (it == clientIdToChannelClient.end())
        {
            return Unexpected(ErrorCode::UnknownClient);
        }
        return it->second->sendBuffer(InvalidClientId, buffer, size);
    }

    std::future<ErrorCode> ChannelUnixSocketServer::start()
    {
        startPromise = std::make_unique<std::promise<ErrorCode>>();
        if (!isStarted())
        {
            stopRequested.store(false);
            serverThread = std::thread([this]()
                                       { this->workThreadFunction(); });
        }
        else
        {
            startPromise->set_value(ErrorCode::AlreadyStarted);
        }
        return startPromise->get_future();
    }

    std::future<void> ChannelUnixSocketServer::stop()
    {
        return std::async(
            std::launch::async,
            [this]()
            {
                stopRequested.store(true);
                if (this->serverThread.joinable())
                {
                    this->serverThread.join();
                }
                this->startPromise = nullptr;
                clientIdToChannelClient.clear();
                socketToClientInfoMapFull.clear();
                clientsObserver.setData(std::vector<ClientInfo>{}, true);
                onStop();
            });
    }

    bool ChannelUnixSocketServer::isStarted() const
    {
        return running.load();
    }

    void ChannelUnixSocketServer::workThreadFunction()
    {
        running.store(true);

        Socket serverSocket = createServerSocket();
        if (serverSocket == InvalidFileDescriptor)
        {
            running.store(false);
            return;
        }
        serverSocketFd = serverSocket;

        pollFds.push_back({serverSocket, POLLIN, 0});
        startPromise->set_value(ErrorCode::Success);

        while (!stopRequested.load())
        {
            int numEvents = poll(pollFds.data(), pollFds.size(), 100);
            if (stopRequested.load())
                break;
            if (numEvents < 0)
            {
                std::cerr << "Poll error: " << strerror(errno) << std::endl;
                break;
            }
            else if (numEvents == 0)
            {
                continue;
            }
            else
            {
                handlePollEvents(serverSocket, numEvents);
                if (stopRequested.load())
                    break;
                processBuffers();
                cleanupClosedSockets();
            }
        }

        ::close(serverSocket);
        serverSocketFd = InvalidFileDescriptor;
        pollFds.clear();
        running.store(false);
        stopRequested.store(false);
    }

    bool ChannelUnixSocketServer::acceptClients(Socket /*serverSocket*/)
    {
        // Default: connection-oriented protocols should override this.
        return false;
    }

    void ChannelUnixSocketServer::readDataFromSocket(Socket clientFd)
    {
        auto it = socketToClientInfoMapFull.find(clientFd);
        if (it == socketToClientInfoMapFull.end())
        {
            socketsToClose.push_back(clientFd);
            return;
        }
        ClientFullInfo &readBuffer = it->second;
        Size leftForRead = readBuffer.buffer.size() - readBuffer.currentSize;
        ssize_t bytesRead = recv(clientFd, readBuffer.buffer.data() + readBuffer.currentSize, leftForRead, 0);
        if (bytesRead > 0)
        {
            readBuffer.currentSize += bytesRead;
            readBuffer.isDirty = true;
        }
        else
        {
            std::cerr << "Error reading from client socket " << clientFd << ": " << strerror(errno) << std::endl;
            socketsToClose.push_back(clientFd);
        }
    }

    void ChannelUnixSocketServer::handlePollEvents(Socket serverSocket, size_t /*numEvents*/)
    {
        for (const auto &pollItem : pollFds)
        {
            if (pollItem.revents & POLLIN)
            {
                if (pollItem.fd == serverSocket)
                {
                    while (acceptClients(serverSocket))
                    {
                        // accept all pending connections
                    }
                }
                else
                {
                    readDataFromSocket(pollItem.fd);
                }
            }
            if (pollItem.revents & (POLLERR | POLLHUP))
            {
                socketsToClose.push_back(pollItem.fd);
                std::cerr << "Error or hang-up on socket " << pollItem.fd << std::endl;
            }
        }
    }

    void ChannelUnixSocketServer::cleanupClosedSockets()
    {
        for (Socket fd : socketsToClose)
        {
            close(fd);
            pollFds.erase(std::remove_if(pollFds.begin(), pollFds.end(),
                                         [fd](const pollfd &p) { return p.fd == fd; }),
                          pollFds.end());
            auto it = std::find_if(
                socketToClientInfoMapFull.begin(),
                socketToClientInfoMapFull.end(),
                [fd](const auto &pair) { return pair.first == fd; });
            if (it != socketToClientInfoMapFull.end())
            {
                clientIdToChannelClient.erase(it->second.id);
            }
            socketToClientInfoMapFull.erase(fd);
        }
        if (!socketsToClose.empty())
        {
            publishClients();
        }
        socketsToClose.clear();
    }

    void ChannelUnixSocketServer::processBuffers()
    {
        const uint8_t headerBytes = to_uint8_t(configuration.packetSizeHeader);
        for (auto &pair : socketToClientInfoMapFull)
        {
            ClientFullInfo &readBuffer = pair.second;
            while (readBuffer.isDirty && readBuffer.currentSize > headerBytes)
            {
                auto packetSizeResult = getPacketSize(
                    configuration.packetSizeHeader,
                    readBuffer.buffer.data(),
                    readBuffer.currentSize);
                if (!packetSizeResult.has_value())
                {
                    // Not enough data even for the header – wait for more.
                    break;
                }
                const Size packetSize = packetSizeResult.value();
                const Size packetSizeWithHeader = packetSize + headerBytes;
                if (readBuffer.currentSize >= packetSizeWithHeader)
                {
                    deserializer->handleBuffer(
                        pair.second.id,
                        readBuffer.buffer.data() + headerBytes,
                        packetSize);
                    Size remainingSize = readBuffer.currentSize - packetSizeWithHeader;
                    if (remainingSize > 0)
                    {
                        std::memmove(readBuffer.buffer.data(),
                                     readBuffer.buffer.data() + packetSizeWithHeader,
                                     remainingSize);
                    }
                    readBuffer.currentSize = remainingSize;
                }
                else
                {
                    // Full payload not yet received – wait for more data.
                    break;
                }
            }
            readBuffer.isDirty = false;
        }
    }

    void ChannelUnixSocketServer::publishClients()
    {
        clientsObserver.setData(
            socketToClientInfoMapFull | std::views::transform([](const auto &pair) {
                return ClientInfo{
                    .id = pair.second.id,
                    .address = pair.second.address,
                    .port = pair.second.port};
            }) | std::ranges::to<std::vector>(),
            false);
    }
} // namespace styxlib
