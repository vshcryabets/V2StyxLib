#include "ChannelDriverTcp.h"
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <stdexcept>

namespace styxlib
{
    ChannelDriverTcpClient::ChannelDriverTcpClient(const Configuration &config)
        : configuration(config), socket(ChannelDriverTcpServer::NO_FD)
    {
    }

    ChannelDriverTcpClient::~ChannelDriverTcpClient()
    {
        disconnect();
    }

    Size ChannelDriverTcpClient::sendBuffer(const StyxBuffer buffer, Size size)
    {
        if (!isConnected())
        {
            return 0;
        }

        // Send the buffer over the TCP socket
        ssize_t bytesSent = send(socket, buffer, size, 0);
        return (bytesSent > 0) ? static_cast<Size>(bytesSent) : 0;
    }

    bool ChannelDriverTcpClient::connect()
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

        return (::connect(socket, reinterpret_cast<sockaddr *>(&serverAddress), sizeof(serverAddress)) == 0);
    }

    void ChannelDriverTcpClient::disconnect()
    {
        if (isConnected())
        {
            close(socket);
            socket = ChannelDriverTcpServer::NO_FD;
        }
    }

    bool ChannelDriverTcpClient::isConnected() const
    {
        return (socket != ChannelDriverTcpServer::NO_FD);
    }

    ChannelDriverTcpServer::ChannelDriverTcpServer(const Configuration &config)
        : configuration(config)
    {
    }

    ChannelDriverTcpServer::~ChannelDriverTcpServer()
    {
        stop();
    }

    Size ChannelDriverTcpServer::sendBuffer(
        int clientId,
        const StyxBuffer buffer,
        Size size)
    {
        if (!isStarted())
        {
            return 0;
        }

        auto it = clients.find(clientId);
        if (it == clients.end())
        {
            return 0;
        }

        ChannelTxPtr &client = it->second;
        return client->sendBuffer(buffer, size);
    }

    void ChannelDriverTcpServer::start()
    {
        if (!isStarted())
        {
            stopRequested.store(false);
            serverThread = std::thread([this]()
                                       { this->workThreadFunction(); });
        }
    }

    std::future<void> ChannelDriverTcpServer::stop()
    {
        stopRequested.store(true);
        return std::async(std::launch::async,
                          [this]()
                          {
                              this->serverThread.join();
                          });
    }
    bool ChannelDriverTcpServer::isStarted() const
    {
        return running.load();
    }

    void ChannelDriverTcpServer::workThreadFunction()
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
            serverSocket = ChannelDriverTcpServer::NO_FD;
            return;
        }

        while (!stopRequested.load())
        {

            if (listen(serverSocket, 1) < 0)
            {
                close(serverSocket);
                serverSocket = ChannelDriverTcpServer::NO_FD;
                return;
            }

            // Accept a client connection (blocking call)
            // clientSocket = accept(serverSocket, nullptr, nullptr);
            // return (clientSocket >= 0);
            // Accept a client connection (blocking call)
            int clientSocket = accept(serverSocket, nullptr, nullptr);
            if (clientSocket >= 0)
            {
                // Create a ChannelTx for the client
                ChannelTxPtr client = std::make_shared<ChannelDriverTcpClient>(
                    ChannelDriverTcpClient::Configuration{
                        .address = "", // Not used for server-side
                        .port = 0      // Not used for server-side
                    });
                // Store the client with its socket as the ID
                clients[clientSocket] = client;
            }
        }
        close(serverSocket);
        serverSocket = ChannelDriverTcpServer::NO_FD;
        running.store(false);
        stopRequested.store(false);
    }
} // namespace styxlib