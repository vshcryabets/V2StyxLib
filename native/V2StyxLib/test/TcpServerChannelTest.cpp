#include "gtest/gtest.h"
#include "server/tcp/TCPServerChannelDriver.h"

TEST(cpp_server_testPrepareSocketWrongPort_test, rw_test) {
	try {
		TCPServerChannelDriver driver("127.0.0.1", 1);
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_BIND_ERROR, err.getInternalCode()) << "Wrong error code";
	}
}

TEST(cpp_server_testPrepareSocketWrongAddress_test, rw_test) {
	try {
		TCPServerChannelDriver driver("1.1.1.1", 1);
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_BIND_ERROR, err.getInternalCode()) << "Wrong error code";
	}
}

TEST(cpp_server_testPrepareSocketIncorrectAddress_test, rw_test) {
	try {
		TCPServerChannelDriver driver("qwewqe", 1);
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket exception should be thrown here";
	} catch (StyxException err) {
		ASSERT_EQ(DRIVER_CANT_RESOLVE_NAME, err.getInternalCode()) << "Wrong error code";
	}
}
