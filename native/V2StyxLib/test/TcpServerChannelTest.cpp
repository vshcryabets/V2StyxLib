#include "gtest/gtest.h"
#include "server/tcp/TCPServerChannelDriver.h"
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include "messages/StyxTVersionMessage.h"
#include "handlers/TMessagesProcessor.h"
#include "handlers/RMessagesProcessor.h"

TEST(cpp_server_testPrepareSocketWrongPort_test, rw_test) {
	try {
		TCPServerChannelDriver driver("localhost", 1);
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_BIND_ERROR, err.getInternalCode()) << "Wrong error code";
	}
}

TEST(cpp_server_testPrepareSocketWrongAddress_test, rw_test) {
	try {
		TCPServerChannelDriver driver("github.com", 10240);
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_BIND_ERROR, err.getInternalCode()) << "Wrong error code";
	}
}

TEST(cpp_server_testPrepareSocketIncorrectAddress_test, rw_test) {
	try {
		TCPServerChannelDriver driver("qwewqe", 10240);
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_CANT_RESOLVE_NAME, err.getInternalCode()) << "Wrong error code";
	}
}

TEST(cpp_server_testSocketBusy, rw_test) {
	TCPServerChannelDriver driver("0.0.0.0", 10240);
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
	uint8_t* senddata;
	uint8_t* recvdata;
	size_t iounit;
	bool checks[2];

	TestServerChannel(uint8_t* indata, uint8_t* outdata, size_t testDataLength) 
		: TCPServerChannelDriver("0.0.0.0", 10240) {
		iounit = 128;
		senddata = indata;
		recvdata = outdata;
		senddata[0] = testDataLength;
		checks[0] = false;
		checks[1] = false;
	}

	StyxMessage* parseMessage(IStyxDataReader* reader) throw(StyxException) {
		size_t length = reader->getUInt32();
		if (length != testdataSize) {
			throw StyxException("Something wrong with received data");
		}
		reader->read(recvdata, length);
		checks[0] = true;
		return new StyxTVersionMessage(iounit, "testAnswer");
	}
};

TEST(cpp_server_testSocketReceive, rw_test) {
	try {
	uint8_t senddata[] = {0, 0, 0, 0, 1, 2};
	uint8_t recvdata[sizeof(senddata)];

	TestServerChannel driver(senddata, recvdata, sizeof(senddata));
	ASSERT_FALSE(driver.checks[0]) << "Wrong check state";
	ASSERT_FALSE(driver.checks[1]) << "Wrong check state";
	
	TMessagesProcessor* tProcessor = new TMessagesProcessor("test1", 
		ConnectionDetails("test", driver.iounit), NULL);
	driver.setTMessageHandler(tProcessor);
			// {
			// 	@Override
			// 	public void addClient(ClientDetails clientDetails) {
			// 		checks[1] = true;
			// 	}
			// });
	driver.setRMessageHandler(new RMessagesProcessor("test2"));
	driver.start(driver.iounit);
	::sleep(1);
	printf("A11\n");

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
	ssize_t result = ::write(sockfd, senddata, sizeof(senddata));
	ASSERT_EQ(sizeof(senddata), result) << "Can't write";
	::sleep(1);

	driver.close();

	ASSERT_TRUE(driver.checks[0]) << "No message for TMessageHandler";
	// ASSERT_TRUE(driver.checks[1]) << "Not called TMessageHandler::addClient";
	// assertArrayEquals(senddata, recvdata, "Wrong recv data");
	} catch (StyxException err) {
		printf("StyxException %s %d\n", err.getMessage().c_str(), err.getInternalCode());
		throw err;
	}

}