/*
 * TCPChannelDriver.cpp
 *
 */

#include "server/tcp/TCPChannelDriver.h"

TCPChannelDriver::TCPChannelDriver(StyxString address, uint16_t port, bool ssl) {
    mPort = port;
    mAddress = address;
    mTransmittedPacketsCount = 0;
    mTransmissionErrorsCount = 0;
}

TCPChannelDriver::~TCPChannelDriver() {
	// TODO Auto-generated destructor stub
}

StyxThread TCPChannelDriver::start(int iounit) {

}

bool TCPChannelDriver::sendMessage(StyxMessage message, ClientDetails *recipient) throw() {

}

void TCPChannelDriver::setTMessageHandler(IMessageProcessor *handler) {
	mTMessageHandler = handler;
}

void TCPChannelDriver::setRMessageHandler(IMessageProcessor *handler) {
	mRMessageHandler = handler;
}

void TCPChannelDriver::close() throw() {

}

size_t TCPChannelDriver::getTransmittedCount() {
	return mTransmittedPacketsCount;
}

size_t TCPChannelDriver::getErrorsCount() {
	return mTransmissionErrorsCount;
}

void TCPChannelDriver::setLogListener(ILogListener *listener) {
	mLogListener = listener;
}

IMessageProcessor* TCPChannelDriver::getTMessageHandler() {
	return mTMessageHandler;
}

IMessageProcessor* TCPChannelDriver::getRMessageHandler() {
	return mRMessageHandler;
}

uint16_t TCPChannelDriver::getPort() {
	return mPort;
}
