#include <catch2/catch_test_macros.hpp>
#include "ChannelDriverTcp.h"
#include <thread>
#include <chrono>

TEST_CASE("testStartStop", "[ChannelDriverTcpServer]")
{
    styxlib::ChannelDriverTcpServer::Configuration config{.port = 23500};
    styxlib::ChannelDriverTcpServer server(config);

    REQUIRE_FALSE(server.isStarted());
    server.start();
    std::this_thread::sleep_for(std::chrono::milliseconds(10));
    REQUIRE(server.isStarted());
    auto future = server.stop();
    auto status = future.wait_for(std::chrono::seconds(1));
    REQUIRE(status == std::future_status::ready);
    REQUIRE_FALSE(server.isStarted());
}