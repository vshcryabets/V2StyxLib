#include <algorithm>
#include <ctime>

#include "gtest/gtest.h"
#include "library/StyxServerManager.h"
#include "server/tcp/TCPServerManager.h"
#include "Connection.h"
#include "vfs/DiskStyxDirectory.h"
#include "server/tcp/TCPClientChannelDriver.h"
#include "platformtools.h"

static const uint16_t PORT = 10234;
static Connection* mConnection;
static StyxServerManager* mServer;

StyxServerManager* startServer() {
	printf("Try to start server\n");
	std::string testDirectory = "./test";
	mServer = new TCPServerManager("127.0.0.1",
			PORT,
			new DiskStyxDirectory(testDirectory));
	mServer->start();
	printf("Server started\n");
	return mServer;
}

void setUp() {
    printf("Setup 1\n");
    startServer();
    Connection::Builder* builder = Connection::Builder()
        .setDriver(new TCPClientChannelDriver("localhost", PORT, "CL1"));
    mConnection = builder->build();
    printf("Setup finished\n");
    ASSERT_TRUE(mConnection->connect());
    printf("Connect finished\n");
}

TEST(cpp_connection_test, connection_test) {
    try {
        setUp();
        size_t count = 1000;
        uint64_t startTime = getTimestampInMilliseconds();
        for ( size_t i = 0; i < count; i++ ) {
            mConnection->sendVersionMessage();
        }
        uint64_t diff = getTimestampInMilliseconds() - startTime;
        printf("\tTransmited %zu messages\n\t"
                "Error %zu messages\n\t"
                "Average time for connection %f ms\n",
                mConnection->getTransmitter()->getTransmittedCount(),
                mConnection->getTransmitter()->getErrorsCount(),
                (double)diff/(double)count
                );
        mConnection->close();
        mServer->close();

        delete mConnection;
        delete mServer;
    } catch (StyxException err) {
        printf("StyxException %s %d\n", err.getMessage().c_str(), err.getInternalCode());
        throw err;
    }
}
