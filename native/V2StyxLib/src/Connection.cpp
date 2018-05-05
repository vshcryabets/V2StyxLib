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

const StyxString Connection::PROTOCOL = StyxString("9P2000");

Connection::Connection(Credentials credentials,
				  IChannelDriver* driver,
				  RMessagesProcessor* answerProcessor,
				  TMessageTransmitter* transmitter,
				  ClientDetails* recepient)
	: mAuthQID(StyxQID::EMPTY), mQID(StyxQID::EMPTY), mCredentials(credentials), mDriver(driver)  {
	init(answerProcessor, transmitter, recepient);
}

Connection::~Connection() {
}

void Connection::init(RMessagesProcessor *answerProcessor,
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
	mDetails = ConnectionDetails(getProtocol(), getIOBufSize());
	isConnectedFlag = false;
}

void Connection::sendVersionMessage() throw(StyxException) {
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
    if ((!mCredentials.getUserName().empty()) && (!mCredentials.getPassword().empty())) {
        sendAuthMessage();
    } else {
        sendAttachMessage();
    }

}

size_t Connection::getIOBufSize() {
	return DEFAULT_IOUNIT;
}

void Connection::sendAuthMessage() throw() {
	mAuthFID = mRecepient->getPolls()->getFIDPoll()->getFreeItem();

	StyxTAuthMessage tAuth(mAuthFID);
	tAuth.setUserName(mCredentials.getUserName());
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
			mCredentials.getUserName(),
			mMountPoint);
	mTransmitter->sendMessage(&tAttach, mRecepient);

	StyxMessage* rMessage = tAttach.waitForAnswer(mTimeout);
	StyxRAttachMessage* rAttach = (StyxRAttachMessage*) rMessage;
	mQID = rAttach->getQID();
	isAttached = true;
}

StyxFID Connection::getRootFID() {
	return mFID;
}

size_t Connection::getTimeout() {
	return mTimeout;
}

IVirtualStyxFile* Connection::getRoot() {
	return mRoot;
}

ConnectionDetails Connection::getConnectionDetails() {
	return mDetails;
}

ClientDetails *Connection::getRecepient() {
	return mRecepient;
}

void Connection::close() throw(StyxException) {
	if (shouldCloseAnswerProcessor && mAnswerProcessor != NULL) {
		mAnswerProcessor->close();
		delete mAnswerProcessor;
		mAnswerProcessor = NULL;
	}
	if (mTransmitter != NULL) {
		mTransmitter->close();
#warning TODO Delete?
		mTransmitter = NULL;
	}
	if (isAutoStartDriver && mDriver != NULL) {
		mDriver->close();
#warning  TODO Delete?
		mDriver = NULL;
	}
}

StyxString Connection::getProtocol() {
	return PROTOCOL;
}

Credentials Connection::getCredentials() {
	return mCredentials;
}

StyxQID Connection::getQID() {
	return mQID;
}

bool Connection::isConnected() {
	return mDriver->isConnected();
}

IMessageTransmitter *Connection::getMessenger() {
	return mTransmitter;
}

StyxString Connection::getMountPoint() {
	return mMountPoint;
}

bool Connection::connect() throw(StyxException) {
	return connect(mDriver, mCredentials);
}

bool Connection::connect(IChannelDriver *driver) throw(StyxException) {
	return connect(driver, mCredentials);
}

bool Connection::connect(IChannelDriver *driver, Credentials credentials) throw(StyxException) {
	if ( mAnswerProcessor == NULL ) {
		mAnswerProcessor = new RMessagesProcessor("RH" + driver->toString());
		shouldCloseAnswerProcessor = true;
	}
	if ( mTransmitter == NULL ) {
		mTransmitter = new TMessageTransmitter(this);
		shouldCloseTransmitter = true;
	}

	if (!driver->isStarted()) {
		driver->start(getIOBufSize());
		isAutoStartDriver = true;
	}

	if (mRecepient == NULL) {
		// get first client from driver
		mRecepient = driver->getClients().front();
	}

	return this->connect(driver, credentials, mAnswerProcessor, mTransmitter, mRecepient);
}

bool Connection::connect(IChannelDriver *driver, Credentials credentials,
		RMessagesProcessor* answerProcessor, TMessageTransmitter* transmitter,
		ClientDetails* recipient) throw(StyxException) {
    if (recipient == NULL) {
        throw StyxException("Recipient can't be null");
    }
    mRecepient = recipient;

    if (transmitter == NULL) {
        throw StyxException("Transmitter can't be null");
    }
    mTransmitter = transmitter;

    if (driver == NULL) {
        throw StyxException("Channel driver can't be null");
    }
    setDriver(driver);

    if (answerProcessor == NULL) {
        throw StyxException("answerProcessor can't be null");
    }
    mAnswerProcessor = answerProcessor;
    mDriver->setRMessageHandler(mAnswerProcessor);

    mCredentials = credentials;
    mMountPoint = "/";
    sendVersionMessage();
    isConnectedFlag = mDriver->isConnected();

    return isConnectedFlag;
}


void Connection::setAttached(bool isAttached) {
	this->isAttached = isAttached;
}


void Connection::onTrashReceived(TMessageTransmitter *caller) {
    //something goes wrong, we should restart protocol
    setAttached(false);
    try {
        sendVersionMessage();
    } catch (StyxException e) {
        e.printStackTrace();
    }
}

void Connection::onSocketDisconnected(TMessageTransmitter *caller) {
	isConnectedFlag = false;
}

void Connection::setDriver(IChannelDriver* driver) {
	mDriver = driver;
}
