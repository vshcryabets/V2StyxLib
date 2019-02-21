#include "gtest/gtest.h"
#include "server/tcp/TCPServerChannelDriver.h"
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include "messages/StyxTVersionMessage.h"
#include "handlers/TMessagesProcessor.h"
#include "handlers/RMessagesProcessor.h"
#include "utils/Log.h"

TEST(cpp_server_testPrepareSocketWrongPort_test, rw_test) {
	try {
		TCPServerChannelDriver driver("localhost", 1, "SRVT1");
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_BIND_ERROR, err.getInternalCode()) << "Wrong error code";
	}
}

TEST(cpp_server_testPrepareSocketWrongAddress_test, rw_test) {
	try {
		TCPServerChannelDriver driver("github.com", 10240, "SRVT1");
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_BIND_ERROR, err.getInternalCode()) << "Wrong error code";
	}
}

TEST(cpp_server_testPrepareSocketIncorrectAddress_test, rw_test) {
	try {
		TCPServerChannelDriver driver("qwewqe", 10240, "SRVT1");
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_CANT_RESOLVE_NAME, err.getInternalCode()) << "Wrong error code";
	}
}

TEST(cpp_server_testSocketBusy, rw_test) {
	TCPServerChannelDriver driver("0.0.0.0", 10240, "SRVT1");
	driver.prepareSocket();
	int test = ::socket(AF_INET, SOCK_STREAM, 0);
	struct hostent *server = ::gethostbyname("127.0.0.1");
    struct sockaddr_in serv_addr;
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr,
        (char *)&serv_addr.sin_addr.s_addr, server->h_length);
    serv_addr.sin_port = htons(10240);
    int result = ::bind(test, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
	ASSERT_NE(0, result) << "Socket error should be there";
	driver.closeSocket();
}

class TestServerChannel : public TCPServerChannelDriver {
public:
	size_t testdataSize;
	size_t recvdataSize;
	uint8_t* senddata;
	uint8_t* recvdata;
	size_t iounit;
	bool messageReceived;

	TestServerChannel(uint8_t* indata, uint8_t* outdata, size_t testDataLength) 
		: TCPServerChannelDriver("0.0.0.0", 10240, "SRVT1"), testdataSize(testDataLength),
			senddata(indata), recvdata(outdata), recvdataSize(0) {
		iounit = 128;
		senddata[0] = testDataLength;
		messageReceived = false;
	}

	StyxMessage* parseMessage(IStyxDataReader* reader) throw(StyxException) {
		size_t length = reader->getUInt32();
		if (length != testdataSize) {
			throw StyxException("Something wrong with received data");
		}
		recvdataSize = reader->read(recvdata, length);
		messageReceived = true;
		return new StyxTVersionMessage(iounit, "testAnswer");
	}
};

class TestTMessagesProcessor : public TMessagesProcessor {
public:
	bool clientAdded;
	TestTMessagesProcessor(ConnectionDetails details) 
		: TMessagesProcessor("test1", details, NULL), clientAdded(false) {
	};
	virtual void addClient(ClientDetails *state) {
		LogDebug("TestTMessagesProcessor client added %p\n", state);
		clientAdded = true;
	};
};

TEST(cpp_server_testSocketReceive, rw_test) {
	uint8_t senddata[] = {0, 0, 0, 0, 1, 2};
	uint8_t recvdata[sizeof(senddata)];
	size_t testDataSize = sizeof(senddata);

	TestServerChannel driver(senddata, recvdata, testDataSize);
	try {
		ASSERT_FALSE(driver.messageReceived) << "Wrong check state";
		
		TestTMessagesProcessor* tProcessor = new TestTMessagesProcessor(
			ConnectionDetails("test", driver.iounit));
		ASSERT_FALSE(tProcessor->clientAdded) << "Wrong check state";
		driver.setTMessageHandler(tProcessor);
		driver.setRMessageHandler(new RMessagesProcessor("test2"));
		driver.start(driver.iounit);
		::sleep(1);

		int sockfd = ::socket(AF_INET, SOCK_STREAM, 0);
		struct hostent *server;
		server = ::gethostbyname("127.0.0.1");
		struct sockaddr_in serverAddress;
		bzero((char *) &serverAddress, sizeof(serverAddress));
		serverAddress.sin_family = AF_INET;
		bcopy((char *)server->h_addr,
			(char *)&serverAddress.sin_addr.s_addr,
			server->h_length);
		serverAddress.sin_port = htons(10240);
		int connectResult = ::connect(sockfd, (struct sockaddr*)&serverAddress, sizeof(serverAddress));
		ASSERT_EQ(0, connectResult) << "Can't connect";
		ssize_t result = ::write(sockfd, senddata, testDataSize);
		ASSERT_EQ(testDataSize, result) << "Can't write";
		::sleep(1);

		ASSERT_TRUE(tProcessor->clientAdded) << "Not called TMessageHandler::addClient";
		ASSERT_TRUE(driver.messageReceived) << "No message for TMessageHandler";
		ASSERT_EQ(testDataSize, driver.recvdataSize) << "Received wrong data size";
		ASSERT_EQ(0, memcmp(recvdata, senddata, testDataSize)) << "Wrong recv data";
	} catch (StyxException err) {
		printf("StyxException %s %d\n", err.getMessage().c_str(), err.getInternalCode());
		throw err;
	}
	driver.closeSocket();
	driver.close();
}