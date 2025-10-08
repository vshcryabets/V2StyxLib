#pragma once

#include "Channel.h"
#include <map>
#include <thread>
#include <atomic>
#include <future>
#include "ClientsRepo.h"
#include "impl/ProgressObservableMutexImpl.h"

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
            DeserializerL4Ptr deserializer{nullptr};
            Configuration(
                const std::string &address, 
                uint16_t port, 
                uint8_t packetSizeHeader,
                uint16_t iounit,
                DeserializerL4Ptr deserializer)
                : address(address), 
                port(port), 
                packetSizeHeader(packetSizeHeader),
                iounit(iounit),
                deserializer(deserializer) {}

            Configuration(
                const std::string &address,
                uint16_t port, 
                int socketFd, 
                uint8_t packetSizeHeader,
                uint16_t iounit,
                DeserializerL4Ptr deserializer)
                : address(address), 
                port(port), 
                socketFd(socketFd), 
                packetSizeHeader(packetSizeHeader),
                iounit(iounit),
                deserializer(deserializer) {}
        };

    private:
        const Configuration configuration;
        int socket;

    public:
        ChannelUnixTcpClient(const Configuration &config);
        ChannelUnixTcpClient(ChannelUnixTcpClient &&) = delete;
        ChannelUnixTcpClient &operator=(ChannelUnixTcpClient &&) = delete;
        ~ChannelUnixTcpClient() override;
        Size sendBuffer(const StyxBuffer buffer, Size size) override;
        std::future<bool> connect();
        std::future<void> disconnect();
        bool isConnected() const;
    };

    class ChannelUnixTcpServer : public ChannelRx, public ChannelTxOneToMany
    {
    public:
        struct Configuration
        {
            uint16_t port;
            std::shared_ptr<ClientsRepo> clientsRepo{nullptr};
            uint8_t packetSizeHeader{4};
            uint16_t iounit{8192};
            DeserializerL4Ptr deserializer{nullptr};
        };
        struct ClientInfo
        {
            ClientId id{0};
            std::string address{""};
            uint16_t port{0};
        };

    private:
        const Configuration configuration;
        std::thread serverThread;
        std::shared_ptr<std::map<int, ClientInfo>> socketToClientInfoMap;
        ProgressObservableMutexImpl<std::shared_ptr<const std::map<int, ClientInfo>>> clientsObserver;
        std::map<ClientId, ChannelTxPtr> clientIdToChannelTx;
        std::atomic<bool> running{false};
        std::atomic<bool> stopRequested{false};
        std::unique_ptr<std::promise<void>> startPromise;

        void workThreadFunction();
        bool acceptClients(int serverSocket);

    public:
        ChannelUnixTcpServer(const Configuration &config);
        ~ChannelUnixTcpServer() override;
        Size sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) override;
        std::future<void> start();
        std::future<void> stop();
        ProgressObserver<std::shared_ptr<const std::map<int, ClientInfo>>> &getClientsObserver()
        {
            return clientsObserver;
        }
        bool isStarted() const;
    };
}