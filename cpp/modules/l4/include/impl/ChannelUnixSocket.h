#pragma once

#include <map>
#include <thread>
#include <atomic>
#include <future>
#include <optional>
#include <string>
#include <poll.h>

#include "Channel.h"
#include "ChannelTx.h"
#include "ClientsRepo.h"
#include "impl/ChannelUnixFile.h"
#include "impl/ProgressObservableMutexImpl.h"

namespace styxlib
{
    using Socket = FileDescriptor;

    /**
     * Common base class for socket-based channel transmitters (TCP, UDP).
     * Holds the socket descriptor and packet header size, and provides
     * a single-call sendBuffer() that prepends the length header and sends
     * header + payload atomically (required for UDP datagrams).
     */
    class ChannelUnixSocketTx : public ChannelTx {
    protected:
        std::optional<Socket> socket = std::nullopt;
        PacketHeaderSize packetSizeHeader{PacketHeaderSize::Size2Bytes};
    public:
        ChannelUnixSocketTx(
            PacketHeaderSize packetSizeHeader, 
            std::optional<Socket> socket
        );
        ChannelUnixSocketTx(const ChannelUnixSocketTx &) = delete;
        ChannelUnixSocketTx(ChannelUnixSocketTx &&) = delete;
        ChannelUnixSocketTx &operator=(ChannelUnixSocketTx &&) = delete;
        ChannelUnixSocketTx &operator=(const ChannelUnixSocketTx &) = delete;
        virtual ~ChannelUnixSocketTx() = default;

        /**
         * Sends the buffer with a length header prepended.
         * Header and payload are combined into one ::send() call so that
         * both TCP and UDP (datagram) transports work correctly.
         * Returns the number of payload bytes sent without the header 
         * (i.e. in success case it should return excactly same value as the size parameter), 
         * or an error code on failure. 
         */
        SizeResult sendBuffer(
            ClientId clientId, 
            const StyxBuffer buffer, 
            Size size) override;
    };

    /**
     * Common base for socket-based client channels (TCP, UDP).
     * Handles the shared Configuration, disconnect(), isConnected(),
     * and the destructor.  Subclasses only need to implement connect(),
     * which creates the protocol-specific socket and calls ::connect().
     */
    class ChannelUnixSocketClient : public ChannelUnixSocketTx, public ChannelRx
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

    protected:
        const Configuration configuration;

    public:
        explicit ChannelUnixSocketClient(const Configuration &config);
        ChannelUnixSocketClient(ChannelUnixSocketClient &&) = delete;
        ChannelUnixSocketClient &operator=(ChannelUnixSocketClient &&) = delete;
        ~ChannelUnixSocketClient() override;

        virtual std::future<ErrorCode> connect() = 0;
        std::future<void> disconnect();
        bool isConnected() const;
    };

    /**
     * Common base for socket-based server channels (TCP, UDP).
     *
     * Handles the shared lifecycle (start / stop / isStarted), the poll loop,
     * client bookkeeping, processBuffers, and cleanupClosedSockets.
     *
     * Subclasses must implement createServerSocket() to set up the protocol-
     * specific listen/bind socket.  Subclasses may also override
     * acceptClients(), readDataFromSocket(), handlePollEvents(),
     * cleanupClosedSockets(), and sendBuffer() to customise per-protocol
     * behaviour (e.g. UDP does not call accept() and uses recvfrom/sendto).
     */
    class ChannelUnixSocketServer : public ChannelRx, public ChannelTx
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
            const uint16_t maxClients{16};

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
                  deserializer(deserializer),
                  maxClients(maxClients)
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
        Socket serverSocketFd{InvalidFileDescriptor};

        // Sockets / pseudo-sockets -> per-client state
        std::map<Socket, ClientFullInfo> socketToClientInfoMapFull;
        std::vector<Socket> socketsToClose;

        // Per-client transmit channels (used by connection-oriented protocols)
        std::map<ClientId, std::shared_ptr<ChannelUnixSocketTx>> clientIdToChannelClient;

        ProgressObservableMutexImpl<std::vector<ClientInfo>> clientsObserver;
        std::atomic<bool> running{false};
        std::atomic<bool> stopRequested{false};
        std::unique_ptr<std::promise<ErrorCode>> startPromise;
        std::vector<pollfd> pollFds;

        // ── Extension points ─────────────────────────────────────────────────
        /**
         * Create, configure and bind (and optionally listen on) the server
         * socket.  Must return a valid fd on success or set startPromise with
         * an error code and return InvalidFileDescriptor on failure.
         */
        virtual Socket createServerSocket() = 0;

        /** Called when the server-socket poll fd fires with POLLIN.
         *  Default: calls accept() in a loop until EAGAIN.
         *  Return true to indicate a new client was accepted (loop continues). */
        virtual bool acceptClients(Socket serverSocket);

        /** Called when a client-socket poll fd fires with POLLIN.
         *  Default: reads via recv() into the client's ReadBuffer. */
        virtual void readDataFromSocket(Socket clientFd);

        /** Dispatch POLLIN / POLLERR / POLLHUP events for all ready fds.
         *  Default implementation: server fd => acceptClients loop,
         *                          client fd => readDataFromSocket,
         *                          POLLERR|POLLHUP => mark socket for closure. */
        virtual void handlePollEvents(Socket serverSocket, size_t numEvents);

        /** Remove sockets that were queued in socketsToClose.
         *  Default: calls ::close(), removes from pollFds and maps. */
        virtual void cleanupClosedSockets();

        /** Process dirty ReadBuffers held in socketToClientInfoMapFull and
         *  forward complete packets to the deserializer.  Not virtual – the
         *  framing logic is identical for all transports. */
        void processBuffers();

        void workThreadFunction();

        /** Helper to rebuild and publish the clients observer vector. */
        void publishClients();

        /** Called at the end of stop() so subclasses can clean up protocol-
         *  specific state (e.g. address maps in the UDP server). */
        virtual void onStop() {}

    public:
        explicit ChannelUnixSocketServer(const Configuration &config);
        ~ChannelUnixSocketServer() override;

        SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) override;
        std::future<ErrorCode> start();
        std::future<void> stop();
        bool isStarted() const;
        ProgressObserver<std::vector<ClientInfo>> &getClientsObserver()
        {
            return clientsObserver;
        }
    };
} // namespace styxlib
