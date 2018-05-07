#include "gtest/gtest.h"
#include <unistd.h>
#include "server/tcp/TCPServerChannelDriver.h"
#include "server/tcp/TCPClientChannelDriver.h"
#include "messages/StyxTVersionMessage.h"
#include "handlers/RMessagesProcessor.h"

TEST(cpp_server_testPrepareSocket_test, rw_test) {
	try {
		TCPServerChannelDriver driver("127.0.0.1", 1);
		driver.prepareSocket();
		ASSERT_TRUE(0) << "Socket excpetion should be thrown here";
	} catch (StyxException err) {
	}
}
