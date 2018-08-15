/*
 * TCPClientDetails.h
 *
 */

#ifndef INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_
#define INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_

#include "server/ClientDetails.h"
#include "io/StyxDataWriter.h"
#include "io/StyxDataReader.h"

class TCPClientDetails : public ClientDetails {
private:
	Socket mChannel;
	StyxBuffer mOutputBuffer;
	StyxDataWriter mOutputWriter;
	StyxByteBufferReadable mInputBuffer;
    StyxDataReader mInputReader;
public:
	TCPClientDetails(Socket socket, IChannelDriver* driver, size_t iounit, int id);
	virtual ~TCPClientDetails();
	Socket getChannel();
	virtual void disconnect() throw(StyxException);
	StyxDataWriter* getOutputWritter();
	StyxBuffer* getOutputBuffer();
	StyxByteBufferReadable* getInputBuffer();
	IStyxDataReader* getInputReader();
};

#endif /* INCLUDE_SERVER_TCP_TCPCLIENTDETAILS_H_ */
