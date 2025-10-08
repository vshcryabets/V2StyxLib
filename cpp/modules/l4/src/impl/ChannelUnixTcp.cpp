#include "ChannelUnixTcp.h"
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <stdexcept>
#include "fcntl.h"
#include <iostream>

namespace styxlib
{
    ChannelUnixTcpClient::ChannelUnixTcpClient(const Configuration &config)
        : ChannelRx("unixClient", config.deserializer), configuration(config)
    {
    }

    ChannelUnixTcpClient::~ChannelUnixTcpClient()
    {
        disconnect();
    }

    SizeResult ChannelUnixTcpClient::sendBuffer(const StyxBuffer buffer, Size size)
    {
        if (socket.has_value())
        {
            // Send the buffer over the TCP socket
            Size bytesSent = ::send(socket.value(), &size, configuration.packetSizeHeader, 0);
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

        auto it = clientIdToChannelTx.find(clientId);
        if (it == clientIdToChannelTx.end())
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
                clientIdToChannelTx.clear();
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

        startPromise->set_value(ErrorCode::Success);

        while (!stopRequested.load())
        {
            auto haveNewClients = acceptClients(serverSocket);
            // read sockets for data here

            std::this_thread::sleep_for(std::chrono::milliseconds(10));
        }
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
        ChannelTxPtr client = std::make_shared<ChannelUnixTcpClient>(
            ChannelUnixTcpClient::Configuration(
                clientInfo.address,
                clientInfo.port,
                clientSocket,
                configuration.packetSizeHeader,
                configuration.iounit,
                configuration.deserializer));
        // Store the client with its socket as the ID
        clientIdToChannelTx[clientInfo.id] = client;
        clientsObserver.setData(socketToClientInfoMap, false);
        return true;
    }

} // namespace styxlib