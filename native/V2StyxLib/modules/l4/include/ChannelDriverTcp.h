#pragma once

#include "ChannelDriver.h"
#include <map>
#include <thread>
#include <atomic>

namespace styxlib
{
    class ChannelDriverTcpClient : public ChannelTx, public ChannelRx
    {
    public:
        struct Configuration
        {
            std::string address;
            uint16_t port;
        };

    private:
        const Configuration configuration;
        int socket;

    public:
        ChannelDriverTcpClient(const Configuration &config);
        ~ChannelDriverTcpClient() override;
        Size sendBuffer(const StyxBuffer buffer, Size size) override;
        bool connect();
        void disconnect();
        bool isConnected() const;
    };

    class ChannelDriverTcpServer : public ChannelRx
    {
    public:
        const static int NO_FD = -1;
        struct Configuration
        {
            uint16_t port;
        };

    private:
        const Configuration configuration;
        std::thread serverThread;
        int serverSocket{NO_FD};
        std::map<int, ChannelTxPtr> clients;
        std::atomic<bool> running{false};
        // int clientSocket;

        void workThreadFunction();

    public:
        ChannelDriverTcpServer(const Configuration &config);
        ~ChannelDriverTcpServer() override;
        Size sendBuffer(int clientId, const StyxBuffer buffer, Size size);
        bool start();
        void stop();
        bool isStarted() const;
    };
}