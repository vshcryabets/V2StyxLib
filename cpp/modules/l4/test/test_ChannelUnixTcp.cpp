#include <catch2/catch_test_macros.hpp>
#include "ChannelUnixTcp.h"
#include <thread>
#include <chrono>
#include "impl/ClientsRepoImpl.h"

TEST_CASE("testStartStop", "[ChannelUnixTcpServer]")
{
    auto clientsRepo = std::make_shared<styxlib::ClientsRepoImpl>();
    styxlib::ChannelUnixTcpServer::Configuration config{
        .port = 23500,
        .clientsRepo = clientsRepo};
    styxlib::ChannelUnixTcpServer server(config);

    REQUIRE_FALSE(server.isStarted());
    auto future = server.start();
    auto status = future.wait_for(std::chrono::seconds(1));
    REQUIRE(status == std::future_status::ready);

    REQUIRE(server.isStarted());

    future = server.stop();
    status = future.wait_for(std::chrono::seconds(1));
    REQUIRE(status == std::future_status::ready);
    REQUIRE_FALSE(server.isStarted());
}

TEST_CASE("testStartConnectDisconnect", "[ChannelUnixTcpServer]")
{
    auto clientsRepo = std::make_shared<styxlib::ClientsRepoImpl>();
    styxlib::ChannelUnixTcpServer::Configuration serverConfig{
        .port = 23500,
        .clientsRepo = clientsRepo};
    styxlib::ChannelUnixTcpServer server(serverConfig);

    REQUIRE(server.start().wait_for(std::chrono::seconds(1)) == std::future_status::ready);

    styxlib::ChannelUnixTcpClient::Configuration clientConfig{
        .address = "127.0.0.1",
        .port = 23500};
    styxlib::ChannelUnixTcpClient client(clientConfig);
    REQUIRE(client.connect().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    REQUIRE(client.isConnected());

    REQUIRE(client.disconnect().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    REQUIRE_FALSE(client.isConnected());
}