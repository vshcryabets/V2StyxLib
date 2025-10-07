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
        : ChannelRx(config.deserializer), configuration(config), socket(ChannelUnixTcpClient::NO_FD)
    {
    }

    ChannelUnixTcpClient::~ChannelUnixTcpClient()
    {
        disconnect();
    }

    Size ChannelUnixTcpClient::sendBuffer(const StyxBuffer buffer, Size size)
    {
        if (!isConnected())
        {
            return 0;
        }

        // Send the buffer over the TCP socket
        ssize_t bytesSent = send(socket, buffer, size, 0);
        return (bytesSent > 0) ? static_cast<Size>(bytesSent) : 0;
    }

    std::future<bool> ChannelUnixTcpClient::connect()
    {
        return std::async(
            std::launch::async, 
            [this] {
                if (isConnected())
                {
                    return true;
                }

                // Create and connect the TCP socket
                int socket = ::socket(AF_INET, SOCK_STREAM, 0);
                if (socket < 0)
                {
                    return false;
                }

                sockaddr_in serverAddress;
                serverAddress.sin_family = AF_INET;
                serverAddress.sin_port = htons(configuration.port);
                inet_pton(AF_INET, configuration.address.c_str(), &serverAddress.sin_addr);
                int result = ::connect(socket, reinterpret_cast<sockaddr *>(&serverAddress), sizeof(serverAddress));
                bool isConnected = result == 0;
                if (isConnected)
                {
                    this->socket = socket;
                }
                else
                {
                    close(socket);
                }
                return isConnected; 
            }
        );
    }

    std::future<void> ChannelUnixTcpClient::disconnect()
    {
        return std::async(
            std::launch::async,
            [this]()
            {
                if (isConnected())
                {
                    close(socket);
                    socket = ChannelUnixTcpClient::NO_FD;
                }
            });
    }

    bool ChannelUnixTcpClient::isConnected() const
    {
        return (socket != ChannelUnixTcpClient::NO_FD);
    }

    ChannelUnixTcpServer::ChannelUnixTcpServer(const Configuration &config)
        : ChannelRx(config.deserializer), configuration(config)
    {
        if (configuration.clientsRepo == nullptr)
        {
            throw std::invalid_argument("ClientsRepo must be provided in configuration");
        }
        socketToClientInfoMap = std::make_shared<std::map<int, ClientInfo>>();
    }

    ChannelUnixTcpServer::~ChannelUnixTcpServer()
    {
        stop();
    }

    Size ChannelUnixTcpServer::sendBuffer(
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

    std::future<void> ChannelUnixTcpServer::start()
    {
        startPromise = std::make_unique<std::promise<void>>();
        if (!isStarted())
        {
            stopRequested.store(false);
            serverThread = std::thread([this]()
                                       { this->workThreadFunction(); });
        }
        else
        {
            startPromise->set_value();
        }
        return startPromise->get_future();
    }

    std::future<void> ChannelUnixTcpServer::stop()
    {
        stopRequested.store(true);
        return std::async(std::launch::async,
                          [this]()
                          {
                              this->startPromise = nullptr;
                              clientIdToChannelTx.clear();
                              socketToClientInfoMap->clear();
                              clientsObserver.setData(socketToClientInfoMap, true);
                              this->serverThread.join();
                          });
    }
    bool ChannelUnixTcpServer::isStarted() const
    {
        return running.load();
    }

    void ChannelUnixTcpServer::workThreadFunction()
    {
        running.store(true);
        startPromise->set_value();
        // Create the server socket
        int serverSocket = ::socket(AF_INET, SOCK_STREAM, 0);
        if (serverSocket < 0)
        {
            return;
        }

        sockaddr_in serverAddress;
        serverAddress.sin_family = AF_INET;
        serverAddress.sin_addr.s_addr = INADDR_ANY;
        serverAddress.sin_port = htons(configuration.port);

        if (bind(serverSocket,
                 reinterpret_cast<sockaddr *>(&serverAddress),
                 sizeof(serverAddress)) < 0)
        {
            close(serverSocket);
            serverSocket = ChannelUnixTcpClient::NO_FD;
            return;
        }

        if (listen(serverSocket, 1) < 0)
        {
            close(serverSocket);
            serverSocket = ChannelUnixTcpClient::NO_FD;
            return;
        }
        // Set server socket to non-blocking mode
        int flags = fcntl(serverSocket, F_GETFL, 0);
        fcntl(serverSocket, F_SETFL, flags | O_NONBLOCK);

        while (!stopRequested.load())
        {
            auto haveNewClients = acceptClients(serverSocket);
            // read sockets for data here

            std::this_thread::sleep_for(std::chrono::milliseconds(10));
        }
        close(serverSocket);
        serverSocket = ChannelUnixTcpClient::NO_FD;
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
        if (getpeername(clientSocket, reinterpret_cast<sockaddr*>(&addr), &addrLen) == 0) {
            char ipStr[INET_ADDRSTRLEN] = {0};
            inet_ntop(AF_INET, &addr.sin_addr, ipStr, sizeof(ipStr));
            clientInfo.address = ipStr;
            clientInfo.port = ntohs(addr.sin_port);
        }

        socketToClientInfoMap->insert({clientSocket, clientInfo});
        // Create a ChannelTx for the client
        ChannelTxPtr client = std::make_shared<ChannelUnixTcpClient>(
            ChannelUnixTcpClient::Configuration{
                .address = clientInfo.address,
                .port = clientInfo.port,
                .socketFd = clientSocket,
                .packetSizeHeader = configuration.packetSizeHeader,
                .iounit = configuration.iounit
            });
        // Store the client with its socket as the ID
        clientIdToChannelTx[clientInfo.id] = client;
        clientsObserver.setData(socketToClientInfoMap, false);
        return true;
    }

} // namespace styxlib