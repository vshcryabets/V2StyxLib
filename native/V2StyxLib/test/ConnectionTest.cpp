#include <algorithm>
#include <ctime>

#include "gtest/gtest.h"
#include "StyxServerManager.h"
#include "server/tcp/TCPServerManager.h"
#include "Connection.h"
#include "vfs/DiskStyxDirectory.h"
#include "server/tcp/TCPClientChannelDriver.h"

static const uint16_t PORT = 10234;
static Connection* mConnection;

StyxServerManager* startServer() {
	std::string testDirectory = "./test";
	StyxServerManager* mServer = new TCPServerManager("127.0.0.1",
			PORT,
			false,
			new DiskStyxDirectory(testDirectory));
	mServer->start();
	return mServer;
}

void setUp() {
	StyxServerManager* server = startServer();
	mConnection = new Connection();
    IChannelDriver* driver = new TCPClientChannelDriver("localhost", PORT, false);
    ASSERT_TRUE(mConnection->connect(driver));
}

TEST(cpp_connection_test, connection_test) {
	setUp();
    size_t count = 1000;
    uint64_t startTime = System.currentTimeMillis();
    for ( size_t i = 0; i < count; i++ ) {
        mConnection->sendVersionMessage();
        printf("Send TVersion %d times\n", i);
    }
    uint64_t diff = System.currentTimeMillis()-startTime;
    printf("\tTransmited %d messages\n\t"
    		"Received %d messages\n\t"
            "Error %d messages\n\t"
            "Average time for connection %d ms",
            mConnection->getMessenger()->getTransmittedCount(),
            mConnection->getMessenger()->getReceivedCount(),
            mConnection->getMessenger()->getErrorsCount(),
            diff/count
            );
    mConnection->close();
}
