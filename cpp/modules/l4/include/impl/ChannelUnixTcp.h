#pragma once

#include <optional>

#include "Channel.h"
#include "ChannelTx.h"
#include "ClientsRepo.h"
#include "impl/ProgressObservableMutexImpl.h"
#include "impl/ChannelUnixSocket.h"

namespace styxlib
{
    /**
     * TCP-specific transmitter. Delegates all send logic to ChannelUnixSocketTx.
     */
    class ChannelUnixTcpTx : public ChannelUnixSocketTx {
    public:
        ChannelUnixTcpTx(
            PacketHeaderSize packetSizeHeader, 
            std::optional<Socket> socket)
            : ChannelUnixSocketTx(packetSizeHeader, socket) {}
        ChannelUnixTcpTx(const ChannelUnixTcpTx &) = delete;
        ChannelUnixTcpTx(ChannelUnixTcpTx &&) = delete;
        ChannelUnixTcpTx &operator=(ChannelUnixTcpTx &&) = delete;
        ChannelUnixTcpTx &operator=(const ChannelUnixTcpTx &) = delete;
        virtual ~ChannelUnixTcpTx() = default;
    };

    class ChannelUnixTcpClient : public ChannelUnixSocketClient
    {
    public:
        // Re-export the base Configuration so existing call sites remain unchanged.
        using Configuration = ChannelUnixSocketClient::Configuration;

        explicit ChannelUnixTcpClient(const Configuration &config);
        ChannelUnixTcpClient(ChannelUnixTcpClient &&) = delete;
        ChannelUnixTcpClient &operator=(ChannelUnixTcpClient &&) = delete;
        ~ChannelUnixTcpClient() override = default;
        std::future<ErrorCode> connect() override;
    };

    /**
     * TCP server channel.
     * Inherits the common server lifecycle, poll loop, buffer processing and
     * client bookkeeping from ChannelUnixSocketServer.  Only the TCP-specific
     * socket setup (SOCK_STREAM / listen) and per-client accept/recv logic
     * is implemented here.
     */
    class ChannelUnixTcpServer : public ChannelUnixSocketServer
    {
    public:
        // Re-export the shared Configuration and ClientInfo types.
        using Configuration = ChannelUnixSocketServer::Configuration;
        using ClientInfo    = ChannelUnixSocketServer::ClientInfo;

        explicit ChannelUnixTcpServer(const Configuration &config);
        ChannelUnixTcpServer(ChannelUnixTcpServer &&) = delete;
        ChannelUnixTcpServer &operator=(ChannelUnixTcpServer &&) = delete;
        ~ChannelUnixTcpServer() override = default;

    protected:
        Socket createServerSocket() override;
        bool   acceptClients(Socket serverSocket) override;
        void   readDataFromSocket(Socket clientFd) override;
    };
}