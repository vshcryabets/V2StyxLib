#include "ChannelUnixTcp.h"
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <stdexcept>
#include "fcntl.h"

namespace styxlib
{
    ChannelUnixTcpClient::ChannelUnixTcpClient(const Configuration &config)
        : configuration(config), socket(ChannelUnixTcpClient::NO_FD)
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
            std::launch::async, [this]
            {
            if (isConnected())
            {
                return true;
            }

            // Create and connect the TCP socket
            socket = ::socket(AF_INET, SOCK_STREAM, 0);
            if (socket < 0)
            {
                return false;
            }

            sockaddr_in serverAddress;
            serverAddress.sin_family = AF_INET;
            serverAddress.sin_port = htons(configuration.port);
            inet_pton(AF_INET, configuration.address.c_str(), &serverAddress.sin_addr);

        return (::connect(socket, reinterpret_cast<sockaddr *>(&serverAddress), sizeof(serverAddress)) == 0); });
    }

    std::future<void> ChannelUnixTcpClient::disconnect()
    {
        if (isConnected())
        {
            close(socket);
            socket = ChannelUnixTcpClient::NO_FD;
        }
    }

    bool ChannelUnixTcpClient::isConnected() const
    {
        return (socket != ChannelUnixTcpClient::NO_FD);
    }

    ChannelUnixTcpServer::ChannelUnixTcpServer(const Configuration &config)
        : configuration(config)
    {
        if (configuration.clientsRepo == nullptr)
        {
            throw std::invalid_argument("ClientsRepo must be provided in configuration");
        }
    }

    ChannelUnixTcpServer::~ChannelUnixTcpServer()
    {
        stop();
    }

    Size ChannelUnixTcpServer::sendBuffer(
        int clientId,
        const StyxBuffer buffer,
        Size size)
    {
        if (!isStarted())
        {
            return 0;
        }

        auto it = socketToChannelTx.find(clientId);
        if (it == socketToChannelTx.end())
        {
            return 0;
        }

        ChannelTxPtr &client = it->second;
        return client->sendBuffer(buffer, size);
    }

    std::future<void> ChannelUnixTcpServer::start()
    {
        if (!isStarted())
        {
            stopRequested.store(false);
            serverThread = std::thread([this]()
                                       { this->workThreadFunction(); });
        }
    }

    std::future<void> ChannelUnixTcpServer::stop()
    {
        stopRequested.store(true);
        return std::async(std::launch::async,
                          [this]()
                          {
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
            // if (errno == EWOULDBLOCK || errno == EAGAIN)
            // {
            //     return;
            //     // // No pending connections, sleep briefly to avoid busy loop
            //     // std::this_thread::sleep_for(std::chrono::milliseconds(10));
            //     // continue;
            // }
            // else
            // {
            //     // Error occurred
            //     // break;
            // }
            return false;
        }
        auto clientId = configuration.clientsRepo->getNextClientId();
        socketToClientId[clientSocket] = clientId;
        // Create a ChannelTx for the client
        ChannelTxPtr client = std::make_shared<ChannelUnixTcpClient>(
            ChannelUnixTcpClient::Configuration{
                .address = "", // Not used for server-side
                .port = 0      // Not used for server-side
            });
        // Store the client with its socket as the ID
        socketToChannelTx[clientSocket] = client;
        return true;
    }

} // namespace styxlib