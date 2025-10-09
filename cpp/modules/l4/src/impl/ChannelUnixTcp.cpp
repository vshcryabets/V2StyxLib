#include "ChannelUnixTcp.h"
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <stdexcept>
#include <fcntl.h>
#include <iostream>
#include <algorithm>

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
        socketToClientInfoMap = std::make_shared<std::map<int, ClientInfo>>();
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
                socketToClientInfoMap->clear();
                clientsObserver.setData(socketToClientInfoMap, true);
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

        // int epollFd = epoll_create1(0);
        // if (epollFd == -1) {
        //     close(serverSocket);
        //     std::cout << "Failed to create epoll instance: " << strerror(errno) << std::endl;
        //     startPromise->set_value(ErrorCode::CantCreateSocketPoll);
        //     return;
        // }
        pollFds.push_back({serverSocket, POLLIN, 0});

        startPromise->set_value(ErrorCode::Success);

        while (!stopRequested.load())
        {
            int num_events = poll(pollFds.data(), pollFds.size(), 100); // 100 ms timeout
            if (num_events < 0)
            {
                std::cerr << "Poll error: " << strerror(errno) << std::endl;
                break;
            } else if (num_events == 0) {
                // Timeout, no events
                continue;
            } else {
                handlePollEvents(serverSocket, num_events);
            }
            
            // auto haveNewClients = acceptClients(serverSocket);
            // // read sockets for data here
            // // If we accepted a new client, we can immediately try to read from all clients
            // // for (auto &[clientId, channelClient] : clientIdToChannelClient)
            // // {
            // //     channelClient->readData();
            // // }
            // if (!haveNewClients) {
            //     std::this_thread::sleep_for(std::chrono::milliseconds(10));
            // }
        }
        // ::close(epollFd);
        ::close(serverSocket);
        running.store(false);
        stopRequested.store(false);
    }

    bool ChannelUnixTcpServer::acceptClients(int serverSocket)
    {
        int clientSocket = accept(serverSocket, nullptr, nullptr);
        if (clientSocket < 0)
        {
            return false;
        }
        ClientInfo clientInfo{
            .id = configuration.clientsRepo->getNextClientId(),
        };
        sockaddr_in addr;
        socklen_t addrLen = sizeof(addr);
        if (getpeername(clientSocket, reinterpret_cast<sockaddr *>(&addr), &addrLen) == 0)
        {
            char ipStr[INET_ADDRSTRLEN] = {0};
            inet_ntop(AF_INET, &addr.sin_addr, ipStr, sizeof(ipStr));
            clientInfo.address = ipStr;
            clientInfo.port = ntohs(addr.sin_port);
        }

        socketToClientInfoMap->insert({clientSocket, clientInfo});
        // Create a ChannelTx for the client
        auto client = std::make_shared<ChannelUnixTcpTx>(configuration.packetSizeHeader, clientSocket);
        // Store the client with its socket as the ID
        clientIdToChannelClient[clientInfo.id] = client;
        clientsObserver.setData(socketToClientInfoMap, false);
        pollFds.push_back({clientSocket, POLLIN | POLLPRI | POLLERR | POLLHUP, 0});
        return true;
    }

    void ChannelUnixTcpServer::handlePollEvents(int serverSocket, size_t numEvents) {
        for (auto &pollItem: pollFds) {
            if (pollItem.revents & POLLIN) {
                if (pollItem.fd == serverSocket) {
                    // New incoming connections
                    while (acceptClients(serverSocket)) ;                    
                } else {
                    int clientFd = pollItem.fd;
                    // Data available on a client socket
                    // Here you would typically read data from the client
                    // For simplicity, we just print a message and continue
                    char buffer[1024];
                    ssize_t bytesRead = recv(clientFd, buffer, sizeof(buffer), 0);
                    if (bytesRead > 0) {
                        std::cout << "Received " << bytesRead << " bytes from client socket " << clientFd << std::endl;
                    } else if (bytesRead == 0) {
                        // Client disconnected
                        std::cout << "Client socket " << clientFd << " disconnected" << std::endl;
                        close(clientFd);
                        pollFds.erase(std::remove_if(pollFds.begin(), pollFds.end(),
                                                        [clientFd](const pollfd &p) { return p.fd == clientFd; }),
                                        pollFds.end());
                    } else {
                        std::cerr << "Error reading from client socket " << clientFd << ": " << strerror(errno) << std::endl;
                    }
                }
                // --num_events;
            } 
            if (pollItem.revents & (POLLERR | POLLHUP)) {
                // Handle error or hang-up
                std::cerr << "Error or hang-up on socket " << pollItem.fd << std::endl;
                // close(pollItem.fd);
                // pollFds.erase(std::remove_if(pollFds.begin(), pollFds.end(),
                //                               [fd=pollItem.fd](const pollfd &p) { return p.fd == fd; }),
                //                pollFds.end());
            }
        }
    }
} // namespace styxlib