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

    // ── ChannelUnixTcpServer ──────────────────────────────────────────────────

    ChannelUnixTcpServer::ChannelUnixTcpServer(const Configuration &config)
        : ChannelUnixSocketServer(config)
    {
    }

    Socket ChannelUnixTcpServer::createServerSocket()
    {
        int serverSocket = ::socket(AF_INET, SOCK_STREAM, 0);
        if (serverSocket < 0)
        {
            startPromise->set_value(ErrorCode::CantCreateSocket);
            return InvalidFileDescriptor;
        }

        int opt = 1;
        if (setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0)
        {
            ::close(serverSocket);
            startPromise->set_value(ErrorCode::CantCreateSocket);
            return InvalidFileDescriptor;
        }

        sockaddr_in serverAddress;
        serverAddress.sin_family = AF_INET;
        serverAddress.sin_addr.s_addr = INADDR_ANY;
        serverAddress.sin_port = htons(configuration.port);

        if (::bind(serverSocket,
                   reinterpret_cast<sockaddr *>(&serverAddress),
                   sizeof(serverAddress)) < 0)
        {
            ::close(serverSocket);
            startPromise->set_value(ErrorCode::CantBindSocket);
            return InvalidFileDescriptor;
        }

        if (::listen(serverSocket, configuration.maxClients) < 0)
        {
            ::close(serverSocket);
            startPromise->set_value(ErrorCode::CantListenSocket);
            return InvalidFileDescriptor;
        }

        // Non-blocking to allow the poll loop to time out gracefully
        int flags = fcntl(serverSocket, F_GETFL, 0);
        fcntl(serverSocket, F_SETFL, flags | O_NONBLOCK);

        return serverSocket;
    }

    bool ChannelUnixTcpServer::acceptClients(Socket serverSocket)
    {
        int clientSocket = ::accept(serverSocket, nullptr, nullptr);
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

        auto client = std::make_shared<ChannelUnixTcpTx>(
            configuration.packetSizeHeader,
            clientSocket);
        clientIdToChannelClient[clientInfo.id] = client;

        publishClients();
        pollFds.push_back({clientSocket, POLLIN | POLLPRI | POLLERR | POLLHUP, 0});
        return true;
    }

    void ChannelUnixTcpServer::readDataFromSocket(Socket clientFd)
    {
        auto it = socketToClientInfoMapFull.find(clientFd);
        if (it == socketToClientInfoMapFull.end())
        {
            socketsToClose.push_back(clientFd);
            return;
        }
        ClientFullInfo &readBuffer = it->second;
        Size leftForRead = readBuffer.buffer.size() - readBuffer.currentSize;
        ssize_t bytesRead = ::recv(clientFd, readBuffer.buffer.data() + readBuffer.currentSize, leftForRead, 0);
        if (bytesRead > 0)
        {
            readBuffer.currentSize += bytesRead;
            readBuffer.isDirty = true;
        }
        else
        {
            std::cerr << "Error reading from TCP client socket " << clientFd << ": " << strerror(errno) << std::endl;
            socketsToClose.push_back(clientFd);
        }
    }
} // namespace styxlib
