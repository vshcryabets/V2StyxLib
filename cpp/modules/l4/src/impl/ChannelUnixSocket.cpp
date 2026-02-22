#include "impl/ChannelUnixSocket.h"

#include <sys/socket.h>
#include <unistd.h>
#include <cstring>
#include <vector>
#include <future>

namespace styxlib
{
    ChannelUnixSocketTx::ChannelUnixSocketTx(
        PacketHeaderSize packetSizeHeader,
        std::optional<Socket> socket)
        : packetSizeHeader(packetSizeHeader), 
        socket(socket)
    {
    }

    SizeResult ChannelUnixSocketTx::sendBuffer(
        ClientId clientId,
        const StyxBuffer buffer,
        Size size)
    {
        if (!socket.has_value())
        {
            return std::unexpected(ErrorCode::NotConnected);
        }

        uint8_t packetSizeBuffer[4] = {0};
        std::expected<uint8_t, ErrorCode> headerSize = setPacketSize(
            packetSizeHeader,
            packetSizeBuffer,
            sizeof(packetSizeBuffer),
            size);
        if (!headerSize.has_value())
        {
            return std::unexpected(headerSize.error());
        }

        // Combine header and payload into a single buffer so the send is
        // atomic. This is essential for UDP (each ::send() maps to one
        // datagram) and harmless for TCP.
        // TODO allocate IOUnit buffer at construction and reuse it here to avoid this copy.
        std::vector<uint8_t> combined(headerSize.value() + size);
        std::memcpy(combined.data(), packetSizeBuffer, headerSize.value());
        std::memcpy(combined.data() + headerSize.value(), buffer, size);
        Size bytesSent = static_cast<Size>(::send(socket.value(), combined.data(), combined.size(), 0));
        return bytesSent - headerSize.value();
    }

    ChannelUnixSocketClient::ChannelUnixSocketClient(const Configuration &config)
        : ChannelUnixSocketTx(config.packetSizeHeader, std::nullopt),
          ChannelRx(),
          configuration(config)
    {
        if (setDeserializer(config.deserializer) != ErrorCode::Success) {
            throw std::invalid_argument("Deserializer cannot be null");
        }
    }

    ChannelUnixSocketClient::~ChannelUnixSocketClient()
    {
        disconnect().get();
    }

    std::future<void> ChannelUnixSocketClient::disconnect()
    {
        return std::async(
            std::launch::async,
            [this]()
            {
                if (socket.has_value())
                {
                    ::close(socket.value());
                    socket = std::nullopt;
                }
            });
    }

    bool ChannelUnixSocketClient::isConnected() const
    {
        return socket.has_value();
    }
} // namespace styxlib
