/*
 * TCPClientDetails.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_
#define INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_

#include "server/ClientDetails.h"

class TCPClientDetails : public ClientDetails {
protected:
	Socket mChannel;
public:
	TCPClientDetails(Socket socket, IChannelDriver* driver, size_t iounit, int id);
	virtual ~TCPClientDetails();
	virtual void disconnect() throw(StyxException);
	Socket getChannel();
};

#endif /* INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_ */
