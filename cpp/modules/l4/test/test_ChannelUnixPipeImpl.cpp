#include <catch2/catch_test_macros.hpp>
#include <thread>
#include <chrono>
#include "impl/ClientsRepoImpl.h"
#include <iostream>
#include <future>
#include <memory>
#include "impl/ChannelUnixPipeImpl.h"
#include "TestClasses.h"

class TestSuiteUnixPipe
{
public:
    constexpr static styxlib::PacketHeaderSize packetSizeHeader = styxlib::PacketHeaderSize::Size1Byte;
    std::shared_ptr<styxlib::ClientsRepoImpl> clientsRepo = std::make_shared<styxlib::ClientsRepoImpl>();
    std::shared_ptr<TestDeserializerL4> clientDeserializer = std::make_shared<TestDeserializerL4>();
    std::shared_ptr<TestDeserializerL4> serverDeserializer = std::make_shared<TestDeserializerL4>();
    std::shared_ptr<styxlib::ChannelUnixPipeImpl> server;
    std::shared_ptr<styxlib::ChannelUnixPipeImpl> client;

public:
    TestSuiteUnixPipe()
    {
        server = std::make_shared<styxlib::ChannelUnixPipeImpl>(
            styxlib::ChannelUnixFile::Configuration(
                packetSizeHeader,
                8192,
                serverDeserializer
            )
        );
        client = std::make_shared<styxlib::ChannelUnixPipeImpl>(
            styxlib::ChannelUnixFile::Configuration(
                packetSizeHeader,
                8192,
                clientDeserializer
            )
        );

        clientDeserializer->setChannelTx(client);
        serverDeserializer->setChannelTx(server);
    }

    void waitStartServer() {
        REQUIRE_FALSE(server->isStarted());
        auto futureWithCode = server->start();
        REQUIRE(futureWithCode.wait_for(std::chrono::seconds(1)) == std::future_status::ready);
        auto expected = futureWithCode.get();
        REQUIRE(expected.has_value());
        REQUIRE(server->isStarted());
    }

    void waitStopServer() {
        REQUIRE(server->isStarted());
        auto future = server->stop();
        REQUIRE(future.wait_for(std::chrono::seconds(1)) == std::future_status::ready);
        future.get();
        REQUIRE_FALSE(server->isStarted());
    }

    void connectClient() {
        auto fds = server->getClientFileDescriptors();
        REQUIRE(fds.readFd != styxlib::InvalidFileDescriptor);
        REQUIRE(fds.writeFd != styxlib::InvalidFileDescriptor);
        auto future = client->connect(fds);
        REQUIRE(future.wait_for(std::chrono::seconds(1)) == std::future_status::ready);
        REQUIRE(future.get() == styxlib::ErrorCode::Success);
        REQUIRE(client->isConnected());
    }
};

TEST_CASE_METHOD(TestSuiteUnixPipe, "ChannelUnixPipeStartStop", "[ChannelUnixPipe]")
{
    waitStartServer();
    waitStopServer();
}

TEST_CASE_METHOD(TestSuiteUnixPipe, "ChannelUnixPipeServerReceiveMessages", "[ChannelUnixPipe]")
{
    waitStartServer();
    connectClient();

    const char *msg = "Hello, World!";
    uint16_t messageSize = strlen(msg);
    auto futureReceivedBytes = serverDeserializer->getReceivedBytes();

    auto bytesSent = client->sendBuffer(styxlib::InvalidClientId, (const styxlib::StyxBuffer)msg, strlen(msg));
    REQUIRE(bytesSent.has_value());
    REQUIRE(bytesSent.value() == messageSize);

    REQUIRE(futureReceivedBytes.wait_for(std::chrono::seconds(2)) == std::future_status::ready);
    REQUIRE(futureReceivedBytes.get() == messageSize);

    REQUIRE(client->stop().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    REQUIRE_FALSE(client->isConnected());
    server->stop().get();
}
