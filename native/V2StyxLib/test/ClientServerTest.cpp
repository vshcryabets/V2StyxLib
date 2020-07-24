#include <algorithm>
#include <ctime>

#include "gtest/gtest.h"
#include "library/StyxServerManager.h"
#include "server/tcp/TCPServerManager.h"
#include "Connection.h"
#include "vfs/DiskStyxDirectory.h"
#include "server/tcp/TCPClientChannelDriver.h"
#include "platformtools.h"
#include <map>

static const uint16_t PORT = 10234;
static const char* ADDRESS = "127.0.0.1";
static StyxServerManager* mServer;
static Connection* mConnection;

class SumStyxFile : public MemoryStyxFile {
private:
    std::map<ClientDetails*, uint32_t> mClientsMap;
public:
    SumStyxFile():MemoryStyxFile("sum") {
        
    }
    virtual bool open(ClientDetails *client, int mode) {
        mClientsMap.insert(std::pair<ClientDetails*, uint32_t>(client, 0));
        return MemoryStyxFile::open(client, mode);
    }

    virtual void close(ClientDetails *client) {
        std::map<ClientDetails*, uint32_t>::iterator it = mClientsMap.find(client);
        if (it != mClientsMap.end()) {
            mClientsMap.erase(it);
        }
        MemoryStyxFile::close(client);
    }

    virtual size_t write(ClientDetails *client, uint8_t* data, uint64_t offset, size_t count) {
        std::map<ClientDetails*, uint32_t>::iterator it = mClientsMap.find(client);
        if (it != mClientsMap.end()) {
            uint32_t sum = 0;
            for (size_t i = 0; i < count; i++) {
                sum += data[i];
            }
            it->second += sum;
        }
        return count;        
    }

    virtual size_t read(ClientDetails *client, uint8_t* buffer, uint64_t offset, size_t count) {
        std::map<ClientDetails*, uint32_t>::iterator it = mClientsMap.find(client);
        if (it != mClientsMap.end()) {
            uint32_t result = it->second;
            //memcpy();
            return sizeof(result);
        }
        return MemoryStyxFile::read(client, buffer, offset, count);        
    }
};

StyxServerManager* startServer() {
    MemoryStyxFile* file = new SumStyxFile();
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
