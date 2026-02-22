#pragma once

#include <optional>
#include <future>

#include "impl/ChannelUnixSocket.h"

namespace styxlib
{
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
} // namespace styxlib
