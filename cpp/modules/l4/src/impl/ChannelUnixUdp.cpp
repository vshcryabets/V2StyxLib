#include "impl/ChannelUnixUdp.h"

#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include <cstring>
#include <future>
#include <iostream>
#include <vector>

#include "Channel.h"

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

    // ── ChannelUnixUdpServer ──────────────────────────────────────────────────

    ChannelUnixUdpServer::ChannelUnixUdpServer(const Configuration &config)
        : ChannelUnixSocketServer(config)
    {
        if (configuration.iounit > UdpMaxPayloadBytes)
        {
            throw std::invalid_argument(
                "iounit (" + std::to_string(configuration.iounit) +
                ") exceeds the maximum UDP payload size for a single Ethernet "
                "frame without fragmentation (" +
                std::to_string(UdpMaxPayloadBytes) + " bytes)");
        }
    }

    Socket ChannelUnixUdpServer::createServerSocket()
    {
        int sock = ::socket(AF_INET, SOCK_DGRAM, 0);
        if (sock < 0)
        {
            startPromise->set_value(ErrorCode::CantCreateSocket);
            return InvalidFileDescriptor;
        }

        int opt = 1;
        if (::setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0)
        {
            ::close(sock);
            startPromise->set_value(ErrorCode::CantCreateSocket);
            return InvalidFileDescriptor;
        }

        sockaddr_in addr{};
        addr.sin_family = AF_INET;
        addr.sin_addr.s_addr = INADDR_ANY;
        addr.sin_port = htons(configuration.port);

        if (::bind(sock, reinterpret_cast<sockaddr *>(&addr), sizeof(addr)) < 0)
        {
            ::close(sock);
            startPromise->set_value(ErrorCode::CantBindSocket);
            return InvalidFileDescriptor;
        }

        // Non-blocking so the poll loop can time out gracefully
        int flags = fcntl(sock, F_GETFL, 0);
        fcntl(sock, F_SETFL, flags | O_NONBLOCK);

        return sock;
    }

    void ChannelUnixUdpServer::handlePollEvents(Socket serverSocket, size_t /*numEvents*/)
    {
        // Each UDP datagram is one complete Styx packet: length-header + payload.
        // There is nothing to accumulate – deliver each datagram to the
        // deserializer immediately after stripping the framing header.
        const uint8_t headerBytes = to_uint8_t(configuration.packetSizeHeader);
        std::vector<uint8_t> datagram(configuration.iounit);

        for (const auto &pollItem : pollFds)
        {
            if (pollItem.revents & POLLIN)
            {
                // Drain all pending datagrams from the single server socket.
                while (true)
                {
                    sockaddr_in peerAddr{};
                    socklen_t addrLen = sizeof(peerAddr);

                    ssize_t bytesRead = ::recvfrom(
                        serverSocket,
                        datagram.data(), datagram.size(),
                        MSG_DONTWAIT,
                        reinterpret_cast<sockaddr *>(&peerAddr), &addrLen);

                    if (bytesRead <= 0)
                        break; // EAGAIN / EWOULDBLOCK – no more datagrams right now

                    if (static_cast<size_t>(bytesRead) <= headerBytes)
                    {
                        std::cerr << "UDP: datagram too short to contain a header – discarded" << std::endl;
                        continue;
                    }
                    const Size payloadSize = static_cast<Size>(bytesRead) - headerBytes;
                    const auto packetSizeResult = getPacketSize(
                        configuration.packetSizeHeader, 
                        datagram.data(), 
                        bytesRead);
                    if (!packetSizeResult.has_value())
                    {
                        std::cerr << "UDP: failed to parse packet size from header – discarded" << 
                            std::endl;
                        continue;
                    }
                    if (packetSizeResult.value() != payloadSize)
                    {
                        std::cerr << "UDP: payload size mismatch (header says " << 
                            packetSizeResult.value() << " bytes, but recvfrom got " << 
                            payloadSize << " bytes) – discarded" << 
                            std::endl;
                        continue;
                    }



                    // ── Identify / register the client ──────────────────────
                    char ipStr[INET_ADDRSTRLEN] = {0};
                    ::inet_ntop(AF_INET, &peerAddr.sin_addr, ipStr, sizeof(ipStr));
                    uint16_t peerPort = ntohs(peerAddr.sin_port);
                    std::string addrKey = std::string(ipStr) + ":" + std::to_string(peerPort);

                    ClientId clientId;
                    auto addrIt = addressToClientId.find(addrKey);
                    if (addrIt == addressToClientId.end())
                    {
                        clientId = configuration.clientsRepo->getNextClientId();
                        addressToClientId[addrKey] = clientId;
                        clientIdToSockAddr[clientId] = peerAddr;

                        // Register in the base map (buffer unused) so that
                        // publishClients() iteration works unchanged.
                        ClientFullInfo info;
                        info.id      = clientId;
                        info.address = ipStr;
                        info.port    = peerPort;
                        // No ReadBuffer needed – UDP datagrams go directly to
                        // the deserializer. isDirty stays false forever so
                        // processBuffers() is a no-op for UDP entries.
                        socketToClientInfoMapFull.insert({static_cast<Socket>(clientId), std::move(info)});
                        publishClients();
                    }
                    else
                    {
                        clientId = addrIt->second;
                    }

                    // ── Deliver payload directly to the deserializer ─────────
                    // Skip the framing header; the remaining bytes are the
                    // complete Styx message payload.
                    deserializer->handleBuffer(
                        clientId,
                        datagram.data() + headerBytes,
                        payloadSize);
                }
            }
            if (pollItem.revents & (POLLERR | POLLHUP))
            {
                std::cerr << "Error on UDP server socket " << pollItem.fd << std::endl;
                stopRequested.store(true);
            }
        }
    }

    void ChannelUnixUdpServer::readDataFromSocket(Socket /*serverFd*/)
    {
        // UDP server reads all datagrams inside handlePollEvents via recvfrom.
        // This override exists only to satisfy the base-class virtual – it is
        // never called in the UDP server code path.
    }

    void ChannelUnixUdpServer::cleanupClosedSockets()
    {
        // UDP has no per-client sockets to close; pseudo-socket keys in
        // socketToClientInfoMapFull are just ClientIds, not real fds.
        socketsToClose.clear();
    }

    SizeResult ChannelUnixUdpServer::sendBuffer(
        ClientId clientId,
        const StyxBuffer buffer,
        Size size)
    {
        if (!isStarted())
            return std::unexpected(ErrorCode::NotConnected);

        auto addrIt = clientIdToSockAddr.find(clientId);
        if (addrIt == clientIdToSockAddr.end())
            return std::unexpected(ErrorCode::UnknownClient);

        const uint8_t headerBytes = to_uint8_t(configuration.packetSizeHeader);
        std::vector<uint8_t> datagram(headerBytes + size);
        auto headerResult = setPacketSize(
            configuration.packetSizeHeader,
            datagram.data(), datagram.size(), size);
        if (!headerResult)
            return std::unexpected(headerResult.error());

        std::memcpy(datagram.data() + headerBytes, buffer, size);

        ssize_t sent = ::sendto(
            serverSocketFd,
            datagram.data(), datagram.size(), 0,
            reinterpret_cast<const sockaddr *>(&addrIt->second),
            sizeof(sockaddr_in));

        if (sent < 0)
            return std::unexpected(ErrorCode::SendFailed);

        // Return the number of payload bytes sent (excluding the header)
        return static_cast<Size>(sent) - headerBytes;
    }

    void ChannelUnixUdpServer::onStop()
    {
        addressToClientId.clear();
        clientIdToSockAddr.clear();
    }
} // namespace styxlib

