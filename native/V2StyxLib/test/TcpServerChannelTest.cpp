#include "gtest/gtest.h"
#include "server/tcp/TCPServerChannelDriver.h"

TEST(cpp_server_testPrepareSocket_test, rw_test) {
	try {
		TCPServerChannelDriver driver("127.0.0.1", 1);
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket excpetion should be thrown here";
	} catch (StyxException err) {
	}
}
