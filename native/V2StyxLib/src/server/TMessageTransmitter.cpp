/*
 * TMessageTransmitter.cpp
 *
 *  Created on: May 5, 2018
 *      Author: vova
 */

#include "server/TMessageTransmitter.h"
#include <sstream>

TMessageTransmitter::TMessageTransmitter()
    :mTransmittedCount(0), mErrorCount(0)
{
}

TMessageTransmitter::~TMessageTransmitter() {
    mListener = NULL;
}

void TMessageTransmitter::setListener(TMessageTransmitter::Listener* listener) 
{
    mListener = listener;
}

bool TMessageTransmitter::sendMessage(StyxMessage *message, ClientDetails *recepient) throw(StyxException) {
    if ( !isMessageTypeTMessage(message->getType()) ) {
        throw StyxException("Can't sent RMessage");
    }
    if (recepient == NULL) {
        throw StyxException("Recepient is null");
    }
    try {
        IChannelDriver* driver = recepient->getDriver();
        if (!driver->isConnected()) throw StyxException("Not connected to server");

        // set message tag
        StyxTAG tag = StyxMessage::NOTAG;
        if (message->getType() != Tversion) {
            tag = recepient->getTagPoll()->getFreeItem();
        }
        message->setTag(tag);
        recepient->putTMessage(tag, (StyxTMessage*) message);

        driver->sendMessage(message, recepient);
        mTransmittedCount++;
        return true;
    } catch (StyxException e) {
        if ( mListener != NULL ) {
            mListener->onChannelDisconnected(this);
        }
    }
    return false;
}

size_t TMessageTransmitter::getTransmittedCount() {
    return mTransmittedCount;
}

size_t TMessageTransmitter::getErrorsCount() {
    return mErrorCount;
}

void TMessageTransmitter::close() throw(StyxException) {

}

StyxString TMessageTransmitter::toString() {
    std::stringstream stream;
	stream << "TMessageTransmitter " <<  std::hex << this;
    return stream.str();
}

StyxMessage* TMessageTransmitter::sendMessageAndWaitAnswer(StyxMessage *message, ClientDetails *recepient, SyncObject* syncObject) 
    throw(StyxException)
{
    if ( !isMessageTypeTMessage(message->getType()) ) {
        throw StyxException("Can't wait answer for RMassage");
    }
    StyxTMessage* tMessage = (StyxTMessage*) message;
    tMessage->setSyncObject(syncObject);
    sendMessage(tMessage, recepient);
    return tMessage->waitForAnswer();
}