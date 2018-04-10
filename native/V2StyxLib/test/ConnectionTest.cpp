#include <algorithm>
#include <ctime>

#include "gtest/gtest.h"
#include "StyxServerManager.h"
#include "server/tcp/TCPServerManager.h"
#include "Connection.h"
#include "vfs/DiskStyxDirectory.h"
#include "server/tcp/TCPClientChannelDriver.h"
#include "platformtools.h"

static const uint16_t PORT = 10234;
static Connection* mConnection;

StyxServerManager* startServer() {
	printf("Try to start server\n");
	std::string testDirectory = "./test";
	StyxServerManager* mServer = new TCPServerManager("127.0.0.1",
			PORT,
			false,
			new DiskStyxDirectory(testDirectory));
	mServer->start();
	printf("Server started\n");
	return mServer;
}

void setUp() {
	printf("Setuo 1\n");
	StyxServerManager* server = startServer();
	mConnection = new Connection();
    IChannelDriver* driver = new TCPClientChannelDriver("localhost", PORT, false);
    printf("Setup finished\n");
    ASSERT_TRUE(mConnection->connect(driver));
}

TEST(cpp_connection_test, connection_test) {
	setUp();
    size_t count = 1000;
    uint64_t startTime = getTimestampInMilliseconds();
    for ( size_t i = 0; i < count; i++ ) {
        mConnection->sendVersionMessage();
    }
    uint64_t diff = getTimestampInMilliseconds() - startTime;
    printf("\tTransmited %zu messages\n\t"
            "Error %zu messages\n\t"
            "Average time for connection %llu ms",
            mConnection->getMessenger()->getTransmittedCount(),
            mConnection->getMessenger()->getErrorsCount(),
            diff/count
            );
    mConnection->close();
}
