#include "gtest/gtest.h"
#include <unistd.h>
#include "server/tcp/TCPServerChannelDriver.h"
#include "server/tcp/TCPClientChannelDriver.h"
#include "messages/StyxTVersionMessage.h"
#include "handlers/RMessagesProcessor.h"

TEST(DISABLED_cpp_server_client_connect_test, rw_test) {
	RMessagesProcessor test1("test1");
	RMessagesProcessor test2("test2");
	RMessagesProcessor test3("test3");
	RMessagesProcessor test4("test4");
	TCPServerChannelDriver server("127.0.0.1", 22345);
	TCPClientChannelDriver client("127.0.0.1", 22345);
	server.setRMessageHandler(&test1);
	server.setTMessageHandler(&test2);
	server.start(128);
	::usleep(200);
	client.setRMessageHandler(&test3);
	client.setTMessageHandler(&test4);
	client.start(96);
	client.sendMessage(new StyxTVersionMessage(96, "TEST"),
			*client.getClients().begin());
}
