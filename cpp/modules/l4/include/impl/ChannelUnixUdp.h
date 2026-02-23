#pragma once

#include <map>
#include <optional>
#include <future>
#include <netinet/in.h>

#include "impl/ChannelUnixSocket.h"

namespace styxlib
{
    /**
     * Maximum UDP payload size that fits in a single Ethernet frame without
     * fragmentation: Ethernet MTU (1500) - IPv4 header (20) - UDP header (8).
     * iounit must not exceed this value.
     */
    constexpr uint16_t UdpMaxPayloadBytes = 1472;

    /**
     * UDP transmitter channel.
     *
     * Uses a "connected" UDP socket (created with SOCK_DGRAM and bound to a
     * remote address via ::connect()) so that the inherited sendBuffer() can
     * use a plain ::send() call, sending header + payload as a single datagram.
     */
    class ChannelUnixUdpTx : public ChannelUnixSocketTx {
    public:
        ChannelUnixUdpTx(
            PacketHeaderSize packetSizeHeader, 
            std::optional<Socket> socket)
            : ChannelUnixSocketTx(packetSizeHeader, socket) {}
        ChannelUnixUdpTx(const ChannelUnixUdpTx &) = delete;
        ChannelUnixUdpTx(ChannelUnixUdpTx &&) = delete;
        ChannelUnixUdpTx &operator=(ChannelUnixUdpTx &&) = delete;
        ChannelUnixUdpTx &operator=(const ChannelUnixUdpTx &) = delete;
        virtual ~ChannelUnixUdpTx() = default;
    };

    /**
     * UDP client channel (Tx + Rx).
     *
     * connect() creates a SOCK_DGRAM socket and calls ::connect() to fix the
     * remote address, enabling plain ::send() / ::recv() calls without
     * specifying the peer on every call.
     */
    class ChannelUnixUdpClient : public ChannelUnixSocketClient
    {
    public:
        using Configuration = ChannelUnixSocketClient::Configuration;

        explicit ChannelUnixUdpClient(const Configuration &config);
        ChannelUnixUdpClient(ChannelUnixUdpClient &&) = delete;
        ChannelUnixUdpClient &operator=(ChannelUnixUdpClient &&) = delete;
        ~ChannelUnixUdpClient() override = default;
        std::future<ErrorCode> connect() override;
    };

    /**
     * UDP server channel.
     *
     * Binds a single SOCK_DGRAM socket to the configured port.  Each unique
     * (source-IP, source-port) pair that sends a datagram is treated as a
     * distinct "client" and assigned a ClientId on first contact.  There is
     * no connection-level acknowledgement – reliability is left to higher
     * protocol layers.
     *
     * Inherits lifecycle management, the poll loop, buffer framing
     * (processBuffers) and the clients observer from ChannelUnixSocketServer.
     * It overrides only the UDP-specific socket setup, datagram reading and
     * send-back logic.
     */
    class ChannelUnixUdpServer : public ChannelUnixSocketServer
    {
    public:
        using Configuration = ChannelUnixSocketServer::Configuration;
        using ClientInfo    = ChannelUnixSocketServer::ClientInfo;

        explicit ChannelUnixUdpServer(const Configuration &config);
        ChannelUnixUdpServer(ChannelUnixUdpServer &&) = delete;
        ChannelUnixUdpServer &operator=(ChannelUnixUdpServer &&) = delete;
        ~ChannelUnixUdpServer() override = default;

        SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) override;

    protected:
        // address string "ip:port" -> assigned ClientId
        std::map<std::string, ClientId> addressToClientId;
        // ClientId -> remote socket address (for sendto)
        std::map<ClientId, sockaddr_in>  clientIdToSockAddr;

        Socket createServerSocket() override;
        void   handlePollEvents(Socket serverSocket, size_t numEvents) override;
        void   readDataFromSocket(Socket serverFd) override;
        void   cleanupClosedSockets() override;
        void   onStop() override;
    };
} // namespace styxlib

