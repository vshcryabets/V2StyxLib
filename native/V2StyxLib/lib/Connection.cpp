#include "Connection.h"
#include "types.h"
#include "messages/base/StyxTMessageFID.h"
#include "messages/StyxTVersionMessage.h"
#include "messages/StyxTAuthMessage.h"
#include "messages/StyxRAuthMessage.h"
#include "messages/StyxRVersionMessage.h"
#include "messages/StyxTAttachMessage.h"
#include "messages/StyxRAttachMessage.h"

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
	mDetails = ConnectionDetails(getProtocol(), getIOBufSize());
	isConnectedFlag = false;
}

void Connection::sendVersionMessage() throw() {
    // release attached FID
    if (mFID != StyxMessage::NOFID) {
        try {
            StyxTMessageFID tClunk(Tclunk, Rclunk, mFID);
            mTransmitter->sendMessage(&tClunk, mRecepient);
        } catch (std::exception e) {
            throw e;
        }
        mFID = StyxMessage::NOFID;
    }

    StyxTVersionMessage tVersion(mDetails.getIOUnit(), getProtocol());
    mTransmitter->sendMessage(&tVersion, mRecepient);

    StyxMessage* rMessage = tVersion.waitForAnswer(mTimeout);
    StyxRVersionMessage* rVersion = (StyxRVersionMessage*) rMessage;
    if (rVersion->getMaxPacketSize() < mDetails.getIOUnit()) {
        mDetails = ConnectionDetails(getProtocol(), rVersion->getMaxPacketSize());
    }
    mRecepient->getPolls()->getFIDPoll()->clean();
    if ((mCredentials.getUserName() != NULL) && (mCredentials.getPassword() != NULL)) {
        sendAuthMessage();
    } else {
        sendAttachMessage();
    }

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

void Connection::sendAuthMessage() throw() {
	mAuthFID = mRecepient->getPolls()->getFIDPoll()->getFreeItem();

	StyxTAuthMessage tAuth(mAuthFID);
	tAuth.setUserName(*mCredentials.getUserName());
	tAuth.setMountPoint(mMountPoint);
	mTransmitter->sendMessage(&tAuth, mRecepient);

	StyxMessage* rMessage = tAuth.waitForAnswer(mTimeout);
	StyxRAuthMessage* rAuth = (StyxRAuthMessage*) rMessage;
	mAuthQID = rAuth->getQID();

	// TODO uncomment later
	//        StyxOutputStream output = new StyxOutputStream((new StyxFile(this,
	//                ((StyxTAuthMessage)tMessage).getAuthFID())).openForWrite());
	//        output.writeString(getPassword());
	//        output.flush();

	sendAttachMessage();
}

void Connection::sendAttachMessage() throw() {
        mFID = mRecepient->getPolls()->getFIDPoll()->getFreeItem();
        StyxTAttachMessage tAttach(getRootFID(), mAuthFID,
                *mCredentials.getUserName(),
                mMountPoint);
        mTransmitter->sendMessage(&tAttach, mRecepient);

        StyxMessage* rMessage = tAttach.waitForAnswer(mTimeout);
        StyxRAttachMessage* rAttach = (StyxRAttachMessage*) rMessage;
        mQID = rAttach->getQID();
        isAttached = true;
    }
