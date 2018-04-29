/*
 * TCPChannelDriver.cpp
 *
 */

#include "server/tcp/TCPChannelDriver.h"
#include "library/StyxServerManager.h"
#include "server/tcp/TCPClientDetails.h"
#include "io/StyxDataWriter.h"
#include <sys/socket.h>

TCPChannelDriver::TCPChannelDriver(StyxString address, uint16_t port, bool ssl) {
    mPort = port;
    mAddress = address;
    mTransmittedPacketsCount = 0;
    mTransmissionErrorsCount = 0;
}

TCPChannelDriver::~TCPChannelDriver() {
	// TODO Auto-generated destructor stub
}

void TCPChannelDriver::setTMessageHandler(IMessageProcessor *handler) {
	mTMessageHandler = handler;
}

void TCPChannelDriver::setRMessageHandler(IMessageProcessor *handler) {
	mRMessageHandler = handler;
}

size_t TCPChannelDriver::getTransmittedCount() {
	return mTransmittedPacketsCount;
}

size_t TCPChannelDriver::getErrorsCount() {
	return mTransmissionErrorsCount;
}

#ifdef USE_LOGGING
void TCPChannelDriver::setLogListener(ILogListener *listener) {
	mLogListener = listener;
}
#endif

IMessageProcessor* TCPChannelDriver::getTMessageHandler() {
	return mTMessageHandler;
}

IMessageProcessor* TCPChannelDriver::getRMessageHandler() {
	return mRMessageHandler;
}

uint16_t TCPChannelDriver::getPort() {
	return mPort;
}

// get connection timeout in miliseconds
size_t TCPChannelDriver::getTimeout() {
	return StyxServerManager::DEFAULT_TIMEOUT;
}

bool TCPChannelDriver::sendMessage(StyxMessage* message, ClientDetails *recipient) throw(StyxException) {
    if ( recipient == NULL ) {
        throw StyxException("Recipient can't be null");
    }
    // TODO may be we can use buffer in stack?
    std::vector<uint8_t>* buffer = ((TCPClientDetails*)recipient)->getOutputBuffer();
    // TODO optimize this line,
	// no need to create new instance
    StyxDataWriter writter(buffer);
	try {
		message->writeToBuffer(&writter);
//            System.out.printf("Limit=%d, %d\n", buffer.limit(), buffer.position());
		Socket channel = ((TCPClientDetails*) recipient)->getChannel();
		::send(channel, buffer, writter.getPosition(), 0);
		mTransmittedPacketsCount++;
#ifdef USE_LOGGING
		if (mLogListener != NULL) {
			mLogListener->onMessageTransmited(this, recipient, message);
		}
#endif
		return true;
	} catch (StyxException e) {
#ifdef USE_LOGGING
		if (mLogListener != null) {
			mLogListener.onException(this, e);
		}
#endif
		mTransmissionErrorsCount++;
	}

    return false;
}
