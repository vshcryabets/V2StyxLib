/*
 * TCPClientState.cpp
 *
 *  Created on: May 2, 2018
 *      Author: vschryabets@gmail.com
 */

#include "server/tcp/TCPClientDetails.h"
#include <sys/socket.h>

TCPClientDetails::TCPClientDetails(Socket socket, IChannelDriver* driver, size_t iounit, int id) 
	: ClientDetails(driver, id), mChannel(socket), mOutputBuffer(StyxBuffer(iounit)),
		mOutputWriter(&mOutputBuffer) {
}

TCPClientDetails::~TCPClientDetails() {
}

Socket TCPClientDetails::getChannel() {
	return mChannel;
}

void TCPClientDetails::disconnect() throw(StyxException) {
#warning TODO something wrong, close should in same place where we have opened it.
	::shutdown(mChannel, SHUT_RDWR);
	mChannel = INVALID_SOCKET;
}

StyxDataWriter* TCPClientDetails::getOutputWritter() {
	return &mOutputWriter;
}

StyxBuffer* TCPClientDetails::getOutputBuffer() {
	return &mOutputBuffer;
}