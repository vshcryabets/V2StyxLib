#include "ChannelUnixTcp.h"
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
        : ChannelRx("unixClient", config.deserializer),
        ChannelUnixTcpTx(config.packetSizeHeader, std::nullopt),
        configuration(config)
    {
    }

    ChannelUnixTcpClient::~ChannelUnixTcpClient()
    {
        disconnect();
    }

    SizeResult ChannelUnixTcpTx::sendBuffer(const StyxBuffer buffer, Size size)
    {
        if (socket.has_value())
        {
            // Send the buffer over the TCP socket
            Size bytesSent = ::send(socket.value(), &size, packetSizeHeader, 0);
            bytesSent += ::send(socket.value(), buffer, size, 0);
            return bytesSent;
        }
        else
        {
            return std::unexpected(ErrorCode::NotConnected);
        }
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

    std::future<void> ChannelUnixTcpClient::disconnect()
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

    bool ChannelUnixTcpClient::isConnected() const
    {
        return socket.has_value();
    }

    ChannelUnixTcpServer::ChannelUnixTcpServer(const Configuration &config)
        : ChannelRx("unixServer", config.deserializer), configuration(config)
    {
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
            return 0;
        }

        auto it = clientIdToChannelClient.find(clientId);
        if (it == clientIdToChannelClient.end())
        {
            return 0;
        }
        return it->second->sendBuffer(buffer, size);
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
                this->startPromise = nullptr;
                clientIdToChannelClient.clear();
                socketToClientInfoMapFull.clear();
                clientsObserver.setData(std::vector<ClientInfo>{}, true);
                if (this->serverThread.joinable())
                {
                    this->serverThread.join();
                }
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

        if (listen(serverSocket, 1) < 0)
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
        auto client = std::make_shared<ChannelUnixTcpTx>(configuration.packetSizeHeader, clientSocket);
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
        std::cout << "handlePollEvents with " << numEvents << " events" << std::endl;
        for (const auto &pollItem: pollFds) {
            if (pollItem.revents & POLLIN) {
                if (pollItem.fd == serverSocket) {
                    // New incoming connections
                    while (acceptClients(serverSocket)) ;                    
                } else {
                    std::cout << "Data available to read on socket " << pollItem.fd << std::endl;
                    readDataFromSocket(pollItem.fd);                    
                }
                // --num_events;
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
        ReadBuffer &readBuffer = it->second;
        Size leftForRead = readBuffer.buffer.size() - readBuffer.currentSize;
        std::cout << "Reading up to " << leftForRead << " bytes from client socket " << clientFd << std::endl;
        ssize_t bytesRead = recv(clientFd, readBuffer.buffer.data() + readBuffer.currentSize, leftForRead, 0);
        if (bytesRead > 0) {
            std::cout << "Read " << bytesRead << " bytes from client socket " << clientFd << std::endl;
            readBuffer.currentSize += bytesRead;
            readBuffer.isDirty = true;
        } else if (bytesRead == 0) {
            std::cerr << "Client socket " << clientFd << " disconnected?" << std::endl;
            socketsToClose.push_back(clientFd);
        } else {
            std::cerr << "Error reading from client socket " << clientFd << ": " << strerror(errno) << std::endl;
            socketsToClose.push_back(clientFd);
        }
    }

    void ChannelUnixTcpServer::cleanupClosedSockets() {
        std::cout << "cleanupClosedSockets() 1"<< std::endl;
        for (int fd : socketsToClose) {
            std::cout << "Cleaning up closed socket " << fd << std::endl;
            close(fd);
            pollFds.erase(std::remove_if(pollFds.begin(), pollFds.end(),
                                          [fd](const pollfd &p) { return p.fd == fd; }),
                           pollFds.end());
            auto clientId = std::find_if(socketToClientInfoMapFull.begin(), socketToClientInfoMapFull.end(),
                                          [fd](const auto &pair) { return pair.first == fd; });
            // TODO what if not found?
            socketToClientInfoMapFull.erase(fd);
            clientIdToChannelClient.erase(clientId->second.id);
        }

        if (socketsToClose.empty() == false) {
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
        for (auto &pair : socketToClientInfoMapFull) {
            ClientFullInfo &readBuffer = pair.second;
            if (readBuffer.isDirty && readBuffer.currentSize > configuration.packetSizeHeader) {
                uint32_t packetSize = 0;
                std::memcpy(&packetSize, readBuffer.buffer.data(), configuration.packetSizeHeader);
                // Convert from network byte order to host byte order
                packetSize = ntohl(packetSize);

                if (readBuffer.currentSize < packetSize) {
                    // Not enough data yet
                    continue;
                }
                // we have whole buffer
                std::cout << "Processing buffer of size " << packetSize << " from socket " << pair.first << std::endl;
                
                // Process the buffer with the deserializer
                auto it = socketToClientInfoMapFull.find(pair.first);
                if (it != socketToClientInfoMapFull.end()) {
                    ClientId clientId = it->second.id;
                    deserializer->handleBuffer(clientId, readBuffer.buffer.data(), readBuffer.currentSize);
                }
                // move buffer
                Size remainingSize = readBuffer.currentSize - packetSize;
                if (remainingSize > 0) {
                    std::memmove(readBuffer.buffer.data(),
                                 readBuffer.buffer.data() + packetSize,
                                 remainingSize);
                }
                readBuffer.currentSize = remainingSize;
                readBuffer.isDirty = false;
            }
        }
    }
} // namespace styxlib