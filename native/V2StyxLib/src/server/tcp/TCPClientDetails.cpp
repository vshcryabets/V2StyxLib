/*
 * TCPClientState.cpp
 *
 *  Created on: May 2, 2018
 *      Author: vschryabets@gmail.com
 */

#include "server/tcp/TCPClientDetails.h"
#include <sys/socket.h>
#include "io/StyxDataReader.h"

TCPClientDetails::TCPClientDetails(Socket socket, IChannelDriver* driver, size_t iounit, int id) 
	: ClientDetails(driver, iounit, id), mChannel(socket) {
	if (socket == INVALID_SOCKET) {
		throw new StyxException("Socket is not ready");
	}
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

