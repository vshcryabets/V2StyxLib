#include <catch2/catch_test_macros.hpp>
#include "ChannelUnixTcp.h"
#include <thread>
#include <chrono>
#include "impl/ClientsRepoImpl.h"
#include <iostream>

class TestDeserializerL4 : public styxlib::DeserializerL4
{
private:
    styxlib::ChannelTxOneToManyPtr _channelTx;
public:
    TestDeserializerL4(styxlib::ChannelTxOneToManyPtr channelTx) : _channelTx(channelTx) {}
    virtual ~TestDeserializerL4() = default;
    void handleBuffer(
        styxlib::ClientId clientId,
        const styxlib::StyxBuffer buffer,
        styxlib::Size size) override
    {
        std::string msg((const char*)buffer, size);
        std::cout << "Received from client " << clientId << ": " << msg << std::endl;
        if (_channelTx)
        {
            const char* response = "Message received";
            _channelTx->sendBuffer(clientId, (const styxlib::StyxBuffer)response, strlen(response));
        }
    }
};

class TestSuite
{
public:
    std::shared_ptr<styxlib::ClientsRepoImpl> clientsRepo = std::make_shared<styxlib::ClientsRepoImpl>();
    styxlib::ChannelUnixTcpServer::Configuration config;
    std::shared_ptr<styxlib::ChannelUnixTcpServer> server;
    styxlib::ChannelUnixTcpClient::Configuration clientConfig;
    std::shared_ptr<styxlib::ChannelUnixTcpClient> client;
    std::shared_ptr<TestDeserializerL4> clientDeserializer = std::make_shared<TestDeserializerL4>();
    std::shared_ptr<TestDeserializerL4> serverDeserializer = std::make_shared<TestDeserializerL4>();

public:
    TestSuite()
    {
        clientsRepo = std::make_shared<styxlib::ClientsRepoImpl>();

        config.clientsRepo = clientsRepo;
        config.port = 23500;
        config.deserializer = serverDeserializer;
        server = std::make_shared<styxlib::ChannelUnixTcpServer>(config);

        clientConfig.address = "127.0.0.1";
        clientConfig.port = 23500;
        clientConfig.deserializer = clientDeserializer;
        client = std::make_shared<styxlib::ChannelUnixTcpClient>(clientConfig);
    }
};

TEST_CASE_METHOD(TestSuite, "Server starts and stops", "[ChannelUnixTcpServer]")
{
    REQUIRE_FALSE(server->isStarted());
    auto future = server->start();
    auto status = future.wait_for(std::chrono::seconds(1));
    REQUIRE(status == std::future_status::ready);

    REQUIRE(server->isStarted());

    future = server->stop();
    status = future.wait_for(std::chrono::seconds(1));
    REQUIRE(status == std::future_status::ready);
    REQUIRE_FALSE(server->isStarted());
}

TEST_CASE_METHOD(TestSuite, "Server accepts connections", "[ChannelUnixTcpServer]")
{
    REQUIRE(server->start().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    REQUIRE(client->connect().wait_for(std::chrono::seconds(1)) == std::future_status::ready);

    auto map = server->getClientsObserver().wait();
    REQUIRE(map->size() == 1);
    std::cout << "Connected clients:" << std::endl;
    for (const auto &[socket, clientInfo] : *map)
    {
        std::cout << "\tSocket: " << socket << ", Client ID: " << clientInfo.id << ", Address: " << clientInfo.address << ", Port: " << clientInfo.port << std::endl;
        REQUIRE(clientInfo.id > 0);
        REQUIRE(clientInfo.port > 0);
        REQUIRE(clientInfo.address.size() > 0);
    }

    REQUIRE(client->isConnected());

    std::cout << "Last issued client ID: " << clientsRepo->getLastIssuedId() << std::endl;

    REQUIRE(client->disconnect().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    REQUIRE_FALSE(client->isConnected());
    server->stop();
    // Wait for observer to complete
    // REQUIRE(observerFuture.wait_for(std::chrono::seconds(1)) == std::future_status::ready);
}

TEST_CASE_METHOD(TestSuite, "Server can receive messages from client", "[ChannelUnixTcpServer]")
{
    REQUIRE(server->start().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    REQUIRE(client->connect().wait_for(std::chrono::seconds(1)) == std::future_status::ready);

    const char *msg = "Hello, World!";
    auto bytesSent = client->sendBuffer((const styxlib::StyxBuffer)msg, strlen(msg));
    REQUIRE(bytesSent == strlen(msg));

    REQUIRE(false);

    REQUIRE(client->disconnect().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    REQUIRE_FALSE(client->isConnected());
    server->stop();
}