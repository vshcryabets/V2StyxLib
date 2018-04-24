/*
 * TCPClientDetails.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_
#define INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_

#include "server/ClientDetails.h"

class TCPClientDetails : public ClientDetails {
private:
	Socket mChannel;
	std::vector<uint8_t>* mOutputBuffer;
protected:
public:
	TCPClientDetails(Socket socket, IChannelDriver* driver, size_t iounit, int id);
	virtual ~TCPClientDetails();
	Socket getChannel();
	virtual void disconnect() throw(StyxException);
	std::vector<uint8_t>* getOutputBuffer();
};

#endif /* INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_ */
