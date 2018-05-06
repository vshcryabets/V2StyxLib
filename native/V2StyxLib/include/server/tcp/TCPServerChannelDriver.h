/*
 * TCPServerChannelDriver.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPSERVERCHANNELDRIVER_H_
#define INCLUDE_SERVER_TCP_TCPSERVERCHANNELDRIVER_H_

#include "server/tcp/TCPChannelDriver.h"

class TCPServerChannelDriver : public TCPChannelDriver {
protected:

public:

	TCPServerChannelDriver(StyxString address, uint16_t port);
	virtual ~TCPServerChannelDriver();

};

#endif /* INCLUDE_SERVER_TCP_TCPSERVERCHANNELDRIVER_H_ */
