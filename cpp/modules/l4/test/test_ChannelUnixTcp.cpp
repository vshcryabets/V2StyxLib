#include <catch2/catch_test_macros.hpp>
#include "ChannelUnixTcp.h"
#include <thread>
#include <chrono>
#include "impl/ClientsRepoImpl.h"
#include <iostream>
#include <future>
#include <memory>
#include "TestClasses.h"

class TestChannelUnixTcpServer: public styxlib::ChannelUnixTcpServer {
public:
    bool dontCallRealMethods = true;
    int acceptCalled = 0;
    int readDataFromSocketCalled = 0;
protected:
    bool acceptClients(int serverSocket) override {
        acceptCalled++;
        if (!dontCallRealMethods)
            return ChannelUnixTcpServer::acceptClients(serverSocket);
        return false;
    }

    void readDataFromSocket(int clientFd) override {
        readDataFromSocketCalled++;
        if (!dontCallRealMethods)
            return ChannelUnixTcpServer::readDataFromSocket(clientFd);
    }

public:
    TestChannelUnixTcpServer()
        : ChannelUnixTcpServer(Configuration(
            23500,
            std::make_shared<styxlib::ClientsRepoImpl>(),
            styxlib::PacketHeaderSize::Size4Bytes,
            8192,
            std::make_shared<TestDeserializerL4OneToMany>(),
            10))
    {
    }
};

class TestSuite
{
public:
    const static styxlib::PacketHeaderSize packetSizeHeader = styxlib::PacketHeaderSize::Size1Byte;
    const static uint16_t port = 23500;
    std::shared_ptr<styxlib::ClientsRepoImpl> clientsRepo = std::make_shared<styxlib::ClientsRepoImpl>();
    std::shared_ptr<TestDeserializerL4OneToMany> clientDeserializer = std::make_shared<TestDeserializerL4OneToMany>();
    std::shared_ptr<TestDeserializerL4OneToMany> serverDeserializer = std::make_shared<TestDeserializerL4OneToMany>();
    styxlib::ChannelUnixTcpServer::Configuration config;
    std::shared_ptr<styxlib::ChannelUnixTcpServer> server;
    styxlib::ChannelUnixTcpClient::Configuration clientConfig;
    std::shared_ptr<styxlib::ChannelUnixTcpClient> client;

public:
    TestSuite(): clientConfig(
            "127.0.0.1",
            port,
            packetSizeHeader,
            8192,
            clientDeserializer
        ), config(
            port,
            clientsRepo,
            packetSizeHeader,
            8192,
            serverDeserializer,
            10
        )
    {
        server = std::make_shared<styxlib::ChannelUnixTcpServer>(config);
        client = std::make_shared<styxlib::ChannelUnixTcpClient>(clientConfig);
        serverDeserializer->setChannelTx(server);
    }

    void waitStartServer() {
        REQUIRE_FALSE(server->isStarted());
        auto futureWithCode = server->start();
        REQUIRE(futureWithCode.wait_for(std::chrono::seconds(1)) == std::future_status::ready);
        REQUIRE(futureWithCode.get() == styxlib::ErrorCode::Success);
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
        auto future = client->connect();
        REQUIRE(future.wait_for(std::chrono::seconds(1)) == std::future_status::ready);
        REQUIRE(future.get() == styxlib::ErrorCode::Success);
        REQUIRE(client->isConnected());
    }
};

TEST_CASE_METHOD(TestSuite, "Server starts and stops", "[ChannelUnixTcpServer]")
{
    waitStartServer();
    waitStopServer();
}

TEST_CASE_METHOD(TestSuite, "Server accepts connections", "[ChannelUnixTcpServer]")
{
    waitStartServer();
    client->connect(); // connect without waiting for future
    auto clientsVector = server->getClientsObserver().wait();
    REQUIRE(clientsVector.size() == 1);
    std::cout << "Connected clients:" << std::endl;
    for (const auto &clientInfo : clientsVector)
    {
        std::cout << "\tClient ID: " << clientInfo.id 
            << ", Address: " << clientInfo.address 
            << ", Port: " << clientInfo.port << std::endl;
        REQUIRE(clientInfo.id > 0);
        REQUIRE(clientInfo.port > 0);
        REQUIRE(clientInfo.address.size() > 0);
    }

    REQUIRE(client->isConnected());
    std::cout << "Last issued client ID: " << clientsRepo->getLastIssuedId() << std::endl;
    REQUIRE(client->disconnect().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    waitStopServer();
}

TEST_CASE_METHOD(TestSuite, "test_ServerReceiveMessages", "[ChannelUnixTcpServer]")
{
    waitStartServer();
    connectClient();

    const char *msg = "Hello, World!";
    uint16_t messageSize = strlen(msg);
    auto futureReceivedBytes = serverDeserializer->getReceivedBytes();

    auto bytesSent = client->sendBuffer((const styxlib::StyxBuffer)msg, strlen(msg));
    REQUIRE(bytesSent.has_value());
    REQUIRE(bytesSent.value() == messageSize);

    REQUIRE(futureReceivedBytes.wait_for(std::chrono::seconds(2)) == std::future_status::ready);
    REQUIRE(futureReceivedBytes.get() == messageSize);

    REQUIRE(client->disconnect().wait_for(std::chrono::seconds(1)) == std::future_status::ready);
    REQUIRE_FALSE(client->isConnected());
    server->stop().get();
}

TEST_CASE_METHOD(TestChannelUnixTcpServer, "handlePollEvents accept connections", "[ChannelUnixTcpServer]")
{
    pollFds.clear();
    pollFds.push_back({ .fd = 1, .events = POLLIN, .revents = POLLIN }); // Simulate server socket ready to accept
    handlePollEvents(1, 1);
    REQUIRE(acceptCalled == 1);
}

TEST_CASE_METHOD(TestChannelUnixTcpServer, "handlePollEvents read data from socket", "[ChannelUnixTcpServer]")
{
    pollFds.clear();
    pollFds.push_back({ .fd = 1, .events = POLLIN, .revents = 0 }); // Simulate server socket ready to accept
    pollFds.push_back({ .fd = 2, .events = POLLIN, .revents = 0 }); // client 1
    pollFds.push_back({ .fd = 3, .events = POLLIN, .revents = POLLIN }); // client 2
    handlePollEvents(1, 1);

    REQUIRE(readDataFromSocketCalled == 1); 
}

TEST_CASE_METHOD(TestChannelUnixTcpServer, "handlePollEvents should cleanup closed sockets", "[ChannelUnixTcpServer]")
{
    dontCallRealMethods = false; // let the real methods be called
    pollFds.clear();
    pollFds.push_back({ .fd = 1, .events = POLLIN, .revents = 0 }); // Simulate server socket ready to accept
    pollFds.push_back({ .fd = 256, .events = POLLIN, .revents = POLLIN }); // client 1
    pollFds.push_back({ .fd = 3, .events = POLLIN, .revents = POLLHUP }); // client 2

    handlePollEvents(1, 2);
    REQUIRE(socketsToClose.size() == 2);

    cleanupClosedSockets();
    REQUIRE(socketsToClose.size() == 0);
    REQUIRE(pollFds.size() == 1);

    REQUIRE(readDataFromSocketCalled == 1); 
}

TEST_CASE_METHOD(TestChannelUnixTcpServer, "test_processBuffers", "[ChannelUnixTcpServer]")
{
    dontCallRealMethods = false; // let the real methods be called

    // Simulate a client with a dirty buffer
    int clientSocket = 42;
    ClientFullInfo clientInfo;
    clientInfo.id = 1;
    clientInfo.address = "127.0.0.1";
    clientInfo.port = 12345;
    clientInfo.buffer = std::vector<uint8_t>(32, 0);
    clientInfo.currentSize = 18; // more than packetSizeHeader
    clientInfo.buffer[0] = 0;
    clientInfo.buffer[1] = 0;
    clientInfo.buffer[2] = 0;
    clientInfo.buffer[3] = 4;
    clientInfo.buffer[4] = 'C';
    clientInfo.buffer[5] = 'A';
    clientInfo.buffer[6] = 'D';
    clientInfo.buffer[7] = 'B';
    // next packet
    clientInfo.buffer[8] = 0x00;
    clientInfo.buffer[9] = 0x00;
    clientInfo.buffer[10] = 0x00;
    clientInfo.buffer[11] = 0x02;
    clientInfo.buffer[12] = 'M';
    clientInfo.buffer[13] = '2';
    // 3rd packet
    clientInfo.buffer[14] = 0x00;
    clientInfo.buffer[15] = 0x00;
    clientInfo.buffer[16] = 0x00;
    clientInfo.buffer[17] = 0x02;
    clientInfo.isDirty = true;
    socketToClientInfoMapFull[clientSocket] = clientInfo;
    // Call processBuffers
    processBuffers();
    // Verify that the buffer is no longer dirty
    REQUIRE(socketToClientInfoMapFull[clientSocket].isDirty == false);
    REQUIRE(socketToClientInfoMapFull[clientSocket].currentSize == 4); // only incomplete 3rd packet remains
    auto testDeserializer = std::dynamic_pointer_cast<TestDeserializerL4OneToMany>(deserializer);
    REQUIRE(testDeserializer->getTotalReceivedBytes() == 6); // 1st packet 4 bytes, 2nd packet 2 bytes
}