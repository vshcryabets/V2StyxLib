/*
 * TCPClientChannelDriver.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPCLIENTCHANNELDRIVER_H_
#define INCLUDE_SERVER_TCP_TCPCLIENTCHANNELDRIVER_H_

#include "server/tcp/TCPChannelDriver.h"

class TCPClientChannelDriver : public TCPChannelDriver {
public:
	TCPClientChannelDriver(StyxString address, uint16_t port, bool ssl);
	virtual ~TCPClientChannelDriver();

	virtual StyxThread start(int iounit);
	virtual void prepareSocket(std::string socketAddress, bool ssl) throw();
	virtual bool isConnected();
	virtual bool isStarted();
	virtual bool sendMessage(StyxMessage message, ClientDetails recipient);
	virtual void run();
	virtual void close() throw();
	virtual std::vector<ClientDetails> getClients();
};

#endif /* INCLUDE_SERVER_TCP_TCPCLIENTCHANNELDRIVER_H_ */
