/*
 * TCPServerChannelDriver.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPSERVERCHANNELDRIVER_H_
#define INCLUDE_SERVER_TCP_TCPSERVERCHANNELDRIVER_H_

#include <queue>
#include <map>
#include "server/tcp/TCPChannelDriver.h"
#include "types.h"

class TCPServerChannelDriver : public TCPChannelDriver {
protected:
	Socket mSocket;
	std::queue<Socket> mNewConnetions;
	std::queue<Socket> mReadable;
	std::map<Socket, ClientDetails*> mClientStatesMap;

	void setNonBlocking(Socket socket);
public:
	TCPServerChannelDriver(StyxString address, uint16_t port);
	virtual ~TCPServerChannelDriver();
	virtual void prepareSocket() throw(StyxException);
	virtual bool isConnected();
	virtual bool isStarted();
	virtual void* run();
	virtual std::vector<ClientDetails*> getClients();
	virtual StyxString toString();
	virtual size_t getMaxPendingQueue();
};

#endif /* INCLUDE_SERVER_TCP_TCPSERVERCHANNELDRIVER_H_ */
