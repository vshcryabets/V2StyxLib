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
	close();
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
	TCPClientDetails* tcpClient = (TCPClientDetails*)recipient;
#warning there shoud be some synchronization, because we can call send message from different threads? and use single output buffer
	try {
		message->writeToBuffer(tcpClient->getOutputWritter());
		::send(tcpClient->getChannel(), tcpClient->getOutputBuffer(), tcpClient->getOutputWritter()->getPosition(), 0);
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

void TCPChannelDriver::close() throw(StyxException) {
	isWorking = false;
	if (mAcceptorThread == NULL) {
		return;
	}
	mAcceptorThread->cancel();
	mAcceptorThread->tryjoin(2000);
	if ( mAcceptorThread->isAlive() ) {
		mAcceptorThread->forceCancel();
	}
	delete mAcceptorThread;
	mAcceptorThread = NULL;
}

StyxThread* TCPChannelDriver::start(size_t iounit) {
		if ( mAcceptorThread != NULL ) {
            throw StyxException("Already started");
        }
        mIOUnit = iounit;
        mAcceptorThread = new StyxThread();
        mAcceptorThread->startRunnable(this);
        isWorking = true;
        return mAcceptorThread;
}
