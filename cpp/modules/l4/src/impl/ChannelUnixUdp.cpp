#include "impl/ChannelUnixUdp.h"

#include <arpa/inet.h>
#include <unistd.h>
#include <future>

namespace styxlib
{
    ChannelUnixUdpClient::ChannelUnixUdpClient(const Configuration &config)
        : ChannelUnixSocketClient(config)
    {
    }

    std::future<ErrorCode> ChannelUnixUdpClient::connect()
    {
        return std::async(
            std::launch::async,
            [this]
            {
                if (isConnected())
                {
                    return ErrorCode::AlreadyStarted;
                }

                // Create a UDP socket.
                int sock = ::socket(AF_INET, SOCK_DGRAM, 0);
                if (sock < 0)
                {
                    return ErrorCode::CantCreateSocket;
                }

                // "Connect" the UDP socket to fix the remote address so that
                // subsequent ::send() calls work without specifying the peer.
                sockaddr_in serverAddress{};
                serverAddress.sin_family = AF_INET;
                serverAddress.sin_port = htons(configuration.port);
                inet_pton(AF_INET, configuration.address.c_str(), &serverAddress.sin_addr);

                int result = ::connect(sock,
                    reinterpret_cast<sockaddr *>(&serverAddress),
                    sizeof(serverAddress));
                if (result == 0)
                {
                    this->socket = sock;
                    return ErrorCode::Success;
                }
                else
                {
                    ::close(sock);
                    return ErrorCode::NotConnected;
                }
            });
    }
} // namespace styxlib
