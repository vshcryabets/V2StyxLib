#pragma once

#include <map>
#include <thread>
#include <atomic>
#include <future>
#include <optional>

#include "Channel.h"
#include "ClientsRepo.h"
#include "impl/ProgressObservableMutexImpl.h"

namespace styxlib
{
    class ChannelUnixTcpTx : public ChannelTx {
    protected:
        std::optional<int> socket = std::nullopt;
        uint8_t packetSizeHeader{4};
    public:
        ChannelUnixTcpTx(uint8_t packetSizeHeader, std::optional<int> socket) : packetSizeHeader(packetSizeHeader), socket(socket) {}
        virtual ~ChannelUnixTcpTx() = default;
        SizeResult sendBuffer(const StyxBuffer buffer, Size size) override;
    };

    class ChannelUnixTcpClient : public ChannelUnixTcpTx, public ChannelRx
    {
    public:
        struct Configuration
        {
            std::string address;
            uint16_t port;
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
        };

    private:
        const Configuration configuration;
    public:
        ChannelUnixTcpClient(const Configuration &config);
        ChannelUnixTcpClient(ChannelUnixTcpClient &&) = delete;
        ChannelUnixTcpClient &operator=(ChannelUnixTcpClient &&) = delete;
        ~ChannelUnixTcpClient() override;
        std::future<ErrorCode> connect();
        std::future<void> disconnect();
        bool isConnected() const;
    };

    class ChannelUnixTcpServer : public ChannelRx, public ChannelTxOneToMany
    {
    public:
        class Configuration
        {
        public:
            const uint16_t port;
            const std::shared_ptr<ClientsRepo> clientsRepo{nullptr};
            const uint8_t packetSizeHeader{4};
            const uint16_t iounit{8192};
            const DeserializerL4Ptr deserializer{nullptr};

            Configuration(
                uint16_t port,
                std::shared_ptr<ClientsRepo> clientsRepo,
                uint8_t packetSizeHeader,
                uint16_t iounit,
                DeserializerL4Ptr deserializer,
                uint8_t maxClients)
                : port(port),
                  clientsRepo(clientsRepo),
                  packetSizeHeader(packetSizeHeader),
                  iounit(iounit),
                  deserializer(deserializer)
            {
            }
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
        std::map<ClientId, std::shared_ptr<ChannelUnixTcpTx>> clientIdToChannelClient;
        std::atomic<bool> running{false};
        std::atomic<bool> stopRequested{false};
        std::unique_ptr<std::promise<ErrorCode>> startPromise;

        void workThreadFunction();
        bool acceptClients(int serverSocket);

    public:
        ChannelUnixTcpServer(const Configuration &config);
        ~ChannelUnixTcpServer() override;
        SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) override;
        std::future<ErrorCode> start();
        std::future<void> stop();
        ProgressObserver<std::shared_ptr<const std::map<int, ClientInfo>>> &getClientsObserver()
        {
            return clientsObserver;
        }
        bool isStarted() const;
    };
}