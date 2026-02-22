#pragma once

#include <optional>

#include "Channel.h"
#include "impl/ChannelUnixFile.h"

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
} // namespace styxlib
