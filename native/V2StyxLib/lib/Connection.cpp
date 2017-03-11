#include "Connection.h"
#include "messages/base/StyxMessage.h"

const size_t Connection::DEFAULT_TIMEOUT = 10000;
const size_t Connection::DEFAULT_IOUNIT = 8192;

Connection::Connection() : mCredentials(Credentials(NULL, NULL)) {
	init(NULL, NULL, NULL, NULL);
}

Connection::Connection(Credentials credentials) : mCredentials(credentials) {
	init(NULL, NULL, NULL, NULL);
}

Connection::Connection(Credentials credentials, IChannelDriver* driver): mCredentials(credentials) {
	init(driver, NULL, NULL, NULL);
}

Connection::Connection(Credentials credentials,
				  IChannelDriver* driver,
				  RMessagesProcessor* answerProcessor,
				  TMessageTransmitter* transmitter,
				  ClientDetails* recepient): mCredentials(credentials) {
	init(driver, answerProcessor, transmitter, recepient);
}

Connection::~Connection() {

}

void Connection::init(IChannelDriver *driver,
        RMessagesProcessor *answerProcessor,
        TMessageTransmitter *transmitter,
        ClientDetails *recepient) {
	mTimeout = DEFAULT_TIMEOUT;
	mAuthFID = StyxMessage::NOFID;
	mFID = StyxMessage::NOFID;
    isAutoStartDriver = false;
    shouldCloseAnswerProcessor = false;
    shouldCloseTransmitter = false;

	mTransmitter = transmitter;
	mRecepient = recepient;
	mAnswerProcessor = answerProcessor;
	mDriver = driver;
	mDetails = new ConnectionDetails(getProtocol(), getIOBufSize());
	isConnectedFlag = false;
}

void Connection::sendVersionMessage() throw() {

}

bool Connection::connect(IChannelDriver *driver) throw() {

}

bool Connection::connect(IChannelDriver *driver, Credentials credentials) throw() {

}

bool Connection::connect() throw() {

}

bool Connection::isConnected() {

}

IMessageTransmitter* Connection::getMessenger() {

}

size_t Connection::getTimeout() {

}

StyxFID Connection::getRootFID() {

}

ConnectionDetails Connection::getConnectionDetails() {

}

ClientDetails* Connection::getRecepient() {

}

void Connection::close() throw() {

}

StyxString Connection::getProtocol() {

}

size_t Connection::getIOBufSize() {
	return DEFAULT_IOUNIT;
}
