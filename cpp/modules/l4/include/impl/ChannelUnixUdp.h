#pragma once

#include <optional>

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
} // namespace styxlib
