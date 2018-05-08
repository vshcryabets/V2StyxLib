/*
 * TCPClientChannelDriver.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPCLIENTCHANNELDRIVER_H_
#define INCLUDE_SERVER_TCP_TCPCLIENTCHANNELDRIVER_H_

#include "server/tcp/TCPChannelDriver.h"
#include "server/tcp/TCPClientDetails.h"

class TCPClientChannelDriver : public TCPChannelDriver {
protected:
	TCPClientDetails* mServerClientDetails;
	Socket mSocket;

public:
	static const int PSEUDO_CLIENT_ID = 1;

	TCPClientChannelDriver(StyxString address, uint16_t port);
	virtual ~TCPClientChannelDriver();

	virtual StyxThread* start(int iounit) throw(StyxException);
	virtual void prepareSocket() throw(StyxException);
	virtual bool isConnected();
	virtual bool isStarted();
	virtual bool sendMessage(StyxMessage* message, ClientDetails *recipient) throw(StyxException);
	virtual void* run();
	virtual void close() throw(StyxException);
	virtual std::vector<ClientDetails*> getClients();
	virtual StyxString toString();
};

#endif /* INCLUDE_SERVER_TCP_TCPCLIENTCHANNELDRIVER_H_ */
