#pragma once

#include "Channel.h"
#include <map>
#include <thread>
#include <atomic>
#include <future>
#include "ClientsRepo.h"

namespace styxlib
{
    class ChannelUnixTcpClient : public ChannelTx, public ChannelRx
    {
    public:
        const static int NO_FD = -1;
        struct Configuration
        {
            std::string address;
            uint16_t port;
            int socketFd = ChannelUnixTcpClient::NO_FD;
            uint8_t packetSizeHeader{4};
            uint16_t iounit{8192};
        };

    private:
        const Configuration configuration;
        int socket;

    public:
        ChannelUnixTcpClient(const Configuration &config);
        ~ChannelUnixTcpClient() override;
        Size sendBuffer(const StyxBuffer buffer, Size size) override;
        std::future<bool> connect();
        std::future<void> disconnect();
        bool isConnected() const;
    };

    class ChannelUnixTcpServer : public ChannelRx
    {
    public:
        struct Configuration
        {
            uint16_t port;
            std::shared_ptr<ClientsRepo> clientsRepo{nullptr};
            uint8_t packetSizeHeader{4};
            uint16_t iounit{8192};
        };

    private:
        const Configuration configuration;
        std::thread serverThread;
        std::map<int, ClientId> socketToClientId;
        std::map<int, ChannelTxPtr> socketToChannelTx;
        std::atomic<bool> running{false};
        std::atomic<bool> stopRequested{false};
        std::unique_ptr<std::promise<void>> startPromise;

        void workThreadFunction();
        bool acceptClients(int serverSocket);

    public:
        ChannelUnixTcpServer(const Configuration &config);
        ~ChannelUnixTcpServer() override;
        Size sendBuffer(int clientId, const StyxBuffer buffer, Size size);
        std::future<void> start();
        std::future<void> stop();
        bool isStarted() const;
    };
}