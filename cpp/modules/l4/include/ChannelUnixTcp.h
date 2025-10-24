#pragma once

#include <map>
#include <thread>
#include <atomic>
#include <future>
#include <optional>
#include <poll.h>

#include "Channel.h"
#include "ClientsRepo.h"
#include "impl/ProgressObservableMutexImpl.h"

namespace styxlib
{
    using Socket = int;

    enum class PacketHeaderSize : uint8_t {
        Size1Byte = 1,
        Size2Bytes = 2,
        Size4Bytes = 4
    };

    inline uint8_t to_uint8_t(const PacketHeaderSize &headerSize) {
        return static_cast<uint8_t>(headerSize);
    }

    struct ReadBuffer {
        std::vector<uint8_t> buffer;
        Size currentSize{0};
        bool isDirty{false};
    };

    class ChannelUnixTcpTx : public ChannelTx {
    protected:
        std::optional<Socket> socket = std::nullopt;
        PacketHeaderSize packetSizeHeader{PacketHeaderSize::Size2Bytes};
    public:
        ChannelUnixTcpTx(PacketHeaderSize packetSizeHeader, std::optional<Socket> socket) : 
            packetSizeHeader(packetSizeHeader), socket(socket) {}
        ChannelUnixTcpTx(const ChannelUnixTcpTx &) = delete;
        ChannelUnixTcpTx(ChannelUnixTcpTx &&) = delete;
        ChannelUnixTcpTx &operator=(ChannelUnixTcpTx &&) = delete;
        ChannelUnixTcpTx &operator=(const ChannelUnixTcpTx &) = delete;
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
            PacketHeaderSize packetSizeHeader{PacketHeaderSize::Size2Bytes};
            uint16_t iounit{8192};
            DeserializerL4Ptr deserializer{nullptr};
            Configuration(
                const std::string &address,
                uint16_t port,
                PacketHeaderSize packetSizeHeader,
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
            const PacketHeaderSize packetSizeHeader{PacketHeaderSize::Size2Bytes};
            const uint16_t iounit{8192};
            const DeserializerL4Ptr deserializer{nullptr};

            Configuration(
                uint16_t port,
                std::shared_ptr<ClientsRepo> clientsRepo,
                PacketHeaderSize packetSizeHeader,
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

    protected:
        class ClientFullInfo : public ClientInfo, public ReadBuffer
        {
        };
        const Configuration configuration;
        std::thread serverThread;
        // Sockets
        std::map<Socket, ClientFullInfo> socketToClientInfoMapFull;
        std::vector<Socket> socketsToClose;
        // Clients
        std::map<ClientId, std::shared_ptr<ChannelUnixTcpTx>> clientIdToChannelClient;

        ProgressObservableMutexImpl<std::vector<ClientInfo>> clientsObserver;
        std::atomic<bool> running{false};
        std::atomic<bool> stopRequested{false};
        std::unique_ptr<std::promise<ErrorCode>> startPromise;
        std::vector<pollfd> pollFds;

        void workThreadFunction();
        virtual bool acceptClients(Socket serverSocket);
        virtual void readDataFromSocket(Socket clientFd);
        void handlePollEvents(Socket serverSocket, size_t numEvents); // Handle poll events
        void cleanupClosedSockets(); // Clean up sockets marked for closure
        void processBuffers(); // Check dirty buffers and send data to deserializer

    public:
        ChannelUnixTcpServer(const Configuration &config);
        ~ChannelUnixTcpServer() override;
        SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) override;
        std::future<ErrorCode> start();
        std::future<void> stop();
        ProgressObserver<std::vector<ClientInfo>> &getClientsObserver()
        {
            return clientsObserver;
        }
        bool isStarted() const;
    };
}