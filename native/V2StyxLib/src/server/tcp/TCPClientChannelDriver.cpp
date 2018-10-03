/*
 * TCPClientChannelDriver.cpp
 *
 */
#include "server/tcp/TCPClientChannelDriver.h"

#include <unistd.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <strings.h>

#include "io/StyxDataReader.h"
#include "exceptions/StyxException.h"

TCPClientChannelDriver::TCPClientChannelDriver(StyxString address, uint16_t port)
	: TCPChannelDriver(address, port), mServerClientDetails(NULL), mSocket(INVALID_SOCKET) {
}

TCPClientChannelDriver::~TCPClientChannelDriver() {
	if (mServerClientDetails != NULL) {
		delete mServerClientDetails;
		mServerClientDetails = NULL;
	}
}

void TCPClientChannelDriver::prepareSocket() throw(StyxException) {
	int sockfd = ::socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
		throw StyxException(DRIVER_CREATE_ERROR);
	}
	struct hostent *server;
	server = ::gethostbyname(mAddress.c_str());
	if (server == NULL) {
		throw StyxException(DRIVER_CANT_RESOLVE_NAME);
	}
	struct sockaddr_in serverAddress;
	bzero((char *) &serverAddress, sizeof(serverAddress));
	serverAddress.sin_family = AF_INET;
	bcopy((char *)server->h_addr,
	      (char *)&serverAddress.sin_addr.s_addr,
	      server->h_length);
	serverAddress.sin_port = htons(mPort);
	int connectResult = ::connect(sockfd, (struct sockaddr*)&serverAddress, sizeof(serverAddress));
	if (connectResult < 0) {
#warning use code here
		throw StyxException("Connect failed %d", errno);
	}

	size_t timeoutMs = getTimeout();
	struct timeval timeout;
	timeout.tv_sec = timeoutMs / 1000;
	timeout.tv_usec = (timeoutMs - timeout.tv_sec * 1000) * 1000;
	if (::setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (struct timeval *)&timeout, sizeof(struct timeval)) != 0) {
		throw StyxException(DRIVER_CONFIGURE_ERROR);
	}
	mSocket = sockfd;
	mServerClientDetails = new TCPClientDetails(mSocket, this, mIOUnit, TCPClientChannelDriver::PSEUDO_CLIENT_ID);
}

bool TCPClientChannelDriver::isConnected() {
	if (mServerClientDetails->getChannel() == INVALID_SOCKET) {
		return false;
	}
	return true;
}

bool TCPClientChannelDriver::isStarted() {
	return isWorking;
}

bool TCPClientChannelDriver::sendMessage(StyxMessage* message, ClientDetails *recipient) throw(StyxException) {
	if (recipient != mServerClientDetails) {
		throw StyxException("Wrong recepient");
	}
	return TCPChannelDriver::sendMessage(message, recipient);
}

void* TCPClientChannelDriver::run() {
	try {
		isWorking = true;
		#warning TODO we can use buffer and reader from mServerClientDetails
		StyxByteBufferReadable buffer(mIOUnit * 2);
		StyxDataReader reader(&buffer);
		while (isWorking) {
			if (mThread->isInterrupted()) break;
			try {
#warning can we remove buffer at all? For example we can try to read 4 bytes from socket/channel, then read data by small portiotion from channel.
				size_t read = buffer.readFromChannel(mSocket);
				if (read > 0) {
					// loop unitl we have unprocessed packets in the input buffer
					while ( buffer.remainsToRead() > 4 ) {
						// try to decode
						uint32_t packetSize = reader.getUInt32();
						if ( buffer.remainsToRead() >= packetSize ) {
#warning somebody should release message
							StyxMessage* message = StyxMessage::factory(&reader, mIOUnit);
#ifdef USE_LOGGING
							if (mLogListener != NULL) {
								mLogListener->onMessageReceived(this, mServerClientDetails, message);
							}
#endif
							if ( isMessageTypeTMessage(message->getType()) ) {
								mTMessageHandler->postPacket(message, mServerClientDetails);
							} else {
								mRMessageHandler->postPacket(message, mServerClientDetails);
							}
						} else {
							break;
						}
					}

				}
			} catch (StyxException exception) {
				throw exception;
			}
		}
	} catch (StyxException exception) {
		mServerClientDetails->disconnect();
		throw exception;
	}
	mServerClientDetails->disconnect();
	isWorking = false;
	return 0;
}

void TCPClientChannelDriver::close() throw(StyxException) {
	#warning empty
	TCPChannelDriver::close();
}

std::vector<ClientDetails*> TCPClientChannelDriver::getClients() {
	std::vector<ClientDetails*> result;
	result.push_back(mServerClientDetails);
	return result;
}

StyxString TCPClientChannelDriver::toString() {
#warning add adress and port here
    return StyxString("TCPClientChannelDriver");
}

void TCPClientChannelDriver::closeSocket() throw(StyxException) {
	if (mSocket == INVALID_SOCKET) {
		throw StyxException(DRIVER_CLOSE_ERROR);
	}
	if (::close(mSocket) != 0) {
		throw StyxException(DRIVER_CLOSE_ERROR);
	}
	mSocket = INVALID_SOCKET;
}