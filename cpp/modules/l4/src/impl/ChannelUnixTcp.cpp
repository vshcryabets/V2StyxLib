#include "impl/ChannelUnixTcp.h"
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <stdexcept>
#include <fcntl.h>
#include <iostream>
#include <algorithm>
#include <ranges>

namespace styxlib
{
    ChannelUnixTcpClient::ChannelUnixTcpClient(const Configuration &config)
        : ChannelUnixSocketClient(config)
    {
    }

    std::future<ErrorCode> ChannelUnixTcpClient::connect()
    {
        return std::async(
            std::launch::async,
            [this]
            {
                if (isConnected())
                {
                    return ErrorCode::AlreadyStarted;
                }

                // Create and connect the TCP socket
                int socket = ::socket(AF_INET, SOCK_STREAM, 0);
                if (socket < 0)
                {
                    return ErrorCode::CantCreateSocket;
                }

                sockaddr_in serverAddress;
                serverAddress.sin_family = AF_INET;
                serverAddress.sin_port = htons(configuration.port);
                inet_pton(AF_INET, configuration.address.c_str(), &serverAddress.sin_addr);
                int result = ::connect(socket, reinterpret_cast<sockaddr *>(&serverAddress), sizeof(serverAddress));
                if (result == 0)
                {
                    this->socket = socket;
                    return ErrorCode::Success;
                }
                else
                {
                    close(socket);
                    return ErrorCode::NotConnected;
                }
            });
    }

    ChannelUnixTcpServer::ChannelUnixTcpServer(const Configuration &config)
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

    ChannelUnixTcpServer::~ChannelUnixTcpServer()
    {
        stop().get();
    }

    SizeResult ChannelUnixTcpServer::sendBuffer(
        ClientId clientId,
        const StyxBuffer buffer,
        Size size)
    {
        if (!isStarted())
        {
            return std::unexpected(ErrorCode::NotConnected);
        }

        auto it = clientIdToChannelClient.find(clientId);
        if (it == clientIdToChannelClient.end())
        {
            return std::unexpected(ErrorCode::UnknownClient);
        }
        return it->second->sendBuffer(InvalidClientId, buffer, size);
    }

    std::future<ErrorCode> ChannelUnixTcpServer::start()
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

    std::future<void> ChannelUnixTcpServer::stop()
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
                // clean structures
                clientIdToChannelClient.clear();
                socketToClientInfoMapFull.clear();
                clientsObserver.setData(std::vector<ClientInfo>{}, true);
            });
    }

    bool ChannelUnixTcpServer::isStarted() const
    {
        return running.load();
    }

    void ChannelUnixTcpServer::workThreadFunction()
    {
        running.store(true);
        // Create the server socket
        int serverSocket = ::socket(AF_INET, SOCK_STREAM, 0);
        if (serverSocket < 0)
        {
            startPromise->set_value(ErrorCode::CantCreateSocket);
            return;
        }

        int opt = 1;
        if (setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0)
        {
            close(serverSocket);
            startPromise->set_value(ErrorCode::CantCreateSocket);
            return;
        }

        sockaddr_in serverAddress;
        serverAddress.sin_family = AF_INET;
        serverAddress.sin_addr.s_addr = INADDR_ANY;
        serverAddress.sin_port = htons(configuration.port);

        if (::bind(serverSocket,
                 reinterpret_cast<sockaddr *>(&serverAddress),
                 sizeof(serverAddress)) < 0)
        {
            close(serverSocket);
            startPromise->set_value(ErrorCode::CantBindSocket);
            return;
        }

        if (::listen(serverSocket, configuration.maxClients) < 0)
        {
            close(serverSocket);
            startPromise->set_value(ErrorCode::CantListenSocket);
            return;
        }
        // Set server socket to non-blocking mode
        int flags = fcntl(serverSocket, F_GETFL, 0);
        fcntl(serverSocket, F_SETFL, flags | O_NONBLOCK);

        pollFds.push_back({serverSocket, POLLIN, 0});

        startPromise->set_value(ErrorCode::Success);

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
                handlePollEvents(serverSocket, num_events);
                if (stopRequested.load())
                    break;
                processBuffers();
                cleanupClosedSockets();
            }
        }
        ::close(serverSocket);
        running.store(false);
        stopRequested.store(false);
    }

    bool ChannelUnixTcpServer::acceptClients(Socket serverSocket)
    {
        int clientSocket = accept(serverSocket, nullptr, nullptr);
        if (clientSocket < 0)
        {
            return false;
        }
        ClientFullInfo clientInfo;
        clientInfo.id = configuration.clientsRepo->getNextClientId();
        clientInfo.buffer = std::vector<uint8_t>(configuration.iounit);
        clientInfo.currentSize = 0;
        clientInfo.isDirty = false;

        sockaddr_in addr;
        socklen_t addrLen = sizeof(addr);
        if (getpeername(clientSocket, reinterpret_cast<sockaddr *>(&addr), &addrLen) == 0)
        {
            char ipStr[INET_ADDRSTRLEN] = {0};
            inet_ntop(AF_INET, &addr.sin_addr, ipStr, sizeof(ipStr));
            clientInfo.address = ipStr;
            clientInfo.port = ntohs(addr.sin_port);
        }

        socketToClientInfoMapFull.insert({clientSocket, clientInfo});
        // Create a ChannelTx for the client
        auto client = std::make_shared<ChannelUnixTcpTx>(
            configuration.packetSizeHeader, 
            clientSocket);
        // Store the client with its socket as the ID
        clientIdToChannelClient[clientInfo.id] = client;
        clientsObserver.setData(
            socketToClientInfoMapFull | std::views::transform([](const auto &pair) { 
                return ClientInfo {
                    .id = pair.second.id,
                    .address = pair.second.address,
                    .port = pair.second.port
                };
            })
            | std::ranges::to<std::vector>(), false);
        pollFds.push_back({clientSocket, POLLIN | POLLPRI | POLLERR | POLLHUP, 0});
        return true;
    }

    void ChannelUnixTcpServer::handlePollEvents(Socket serverSocket, size_t numEvents) {
        for (const auto &pollItem: pollFds) {
            if (pollItem.revents & POLLIN) {
                if (pollItem.fd == serverSocket) {
                    // New incoming connections
                    while (acceptClients(serverSocket)) {
                        // Accept all pending connections
                    }
                } else {
                    readDataFromSocket(pollItem.fd);
                }
            } 
            if (pollItem.revents & (POLLERR | POLLHUP)) {
                socketsToClose.push_back(pollItem.fd);
                std::cerr << "Error or hang-up on socket " << pollItem.fd << std::endl;
            }
        }
    }

    void ChannelUnixTcpServer::readDataFromSocket(Socket clientFd) {
        auto it = socketToClientInfoMapFull.find(clientFd);
        if (it == socketToClientInfoMapFull.end()) {
            socketsToClose.push_back(clientFd);
            return;
        }
        ClientFullInfo &readBuffer = it->second;
        Size leftForRead = readBuffer.buffer.size() - readBuffer.currentSize;
        ssize_t bytesRead = recv(clientFd, readBuffer.buffer.data() + readBuffer.currentSize, leftForRead, 0);
        if (bytesRead > 0) {
            readBuffer.currentSize += bytesRead;
            readBuffer.isDirty = true;
        } else {
            std::cerr << "Error reading from client socket " << clientFd << ": " << strerror(errno) << std::endl;
            socketsToClose.push_back(clientFd);
        }
    }

    void ChannelUnixTcpServer::cleanupClosedSockets() {
        for (Socket fd : socketsToClose) {
            close(fd);
            pollFds.erase(std::remove_if(pollFds.begin(), pollFds.end(),
                                          [fd](const pollfd &p) { return p.fd == fd; }),
                           pollFds.end());
            auto it = std::find_if(
                socketToClientInfoMapFull.begin(), 
                socketToClientInfoMapFull.end(),
                [fd](const auto &pair) { return pair.first == fd; });
            
            if (it != socketToClientInfoMapFull.end()) {
                clientIdToChannelClient.erase(it->second.id);
            }
            socketToClientInfoMapFull.erase(fd);
        }

        if (!socketsToClose.empty()) {
            clientsObserver.setData(socketToClientInfoMapFull 
                | std::views::transform([](const auto &pair) { 
                    return ClientInfo {
                        .id = pair.second.id,
                        .address = pair.second.address,
                        .port = pair.second.port
                    };
                })
                | std::ranges::to<std::vector>(), false);
        }
        socketsToClose.clear();
    }

    void ChannelUnixTcpServer::processBuffers() {
        auto headerSize = to_uint8_t(configuration.packetSizeHeader);
        for (auto &pair : socketToClientInfoMapFull) {
            ClientFullInfo &readBuffer = pair.second;
            while (readBuffer.isDirty && readBuffer.currentSize > headerSize) {
                uint32_t packetSize = 0;
                switch (configuration.packetSizeHeader)
                {
                case PacketHeaderSize::Size1Byte:
                    packetSize = readBuffer.buffer[0];
                    break;
                case PacketHeaderSize::Size2Bytes:
                    packetSize = readBuffer.buffer[1] | (readBuffer.buffer[0] << 8);
                    break;
                case PacketHeaderSize::Size4Bytes:
                    packetSize = readBuffer.buffer[3] | 
                        (readBuffer.buffer[2] << 8) | 
                        (readBuffer.buffer[1] << 16) | 
                        (readBuffer.buffer[0] << 24);
                    break;
                }
                uint32_t packetSizeWithHeader = packetSize + to_uint8_t(configuration.packetSizeHeader);
                if (readBuffer.currentSize >= packetSizeWithHeader) {                    
                    // Process the buffer with the deserializer
                    auto it = socketToClientInfoMapFull.find(pair.first);
                    if (it != socketToClientInfoMapFull.end()) {
                        ClientId clientId = it->second.id;
                        deserializer->handleBuffer(clientId, readBuffer.buffer.data() + to_uint8_t(configuration.packetSizeHeader), packetSize);
                    }
                    // move buffer
                    Size remainingSize = readBuffer.currentSize - packetSizeWithHeader;
                    if (remainingSize > 0) {
                        std::memmove(readBuffer.buffer.data(),
                                    readBuffer.buffer.data() + packetSizeWithHeader,
                                    remainingSize);
                    }
                    readBuffer.currentSize = remainingSize;
                } else {
                    // Not enough data to process complete packet; waiting for more data
                    break;
                }
            }
            readBuffer.isDirty = false;
        }
    }
} // namespace styxlib