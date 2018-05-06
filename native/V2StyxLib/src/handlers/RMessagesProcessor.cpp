/*
 * TMessagesProcessor.cpp
 *
 *  Created on: Apr 13, 2018
 *      Author: vshcryabets@gmail.com
 */
#include "handlers/RMessagesProcessor.h"

RMessagesProcessor::RMessagesProcessor(StyxString tag)
	: QueueMessagesProcessor(tag), mTag(tag) {
}

RMessagesProcessor::~RMessagesProcessor() {
	// TODO Auto-generated destructor stub
}

void RMessagesProcessor::removeClient(ClientDetails *state) {
	// nothing to do
}

void RMessagesProcessor::processPacket(StyxMessage *message, ClientDetails *client) throw(StyxException) {
	mReceivedCount++;
	StyxTAG tag = message->getTag();
	StyxTMessage* tMessage = client->getPolls()->getTMessage(tag);
	if (tMessage == NULL) {
		// we didn't send T message with such tag, so ignore this R message
		throw StyxException("RMP(%s) got unknown R message from client %s",
				mTag.c_str(),
				client->toString().c_str());
	}
	// TODO i'm not sure that this is proper place for that logic
	if (tMessage->getType() == Tclunk ||
			tMessage->getType() == Tremove) {
		client->getPolls()->releaseFID((StyxTMessageFID*) tMessage);
	}
	try {
		tMessage->setAnswer(message);
	} catch (StyxException e) {
		e.printStackTrace();
	}
	if (message->getType() == Rerror) {
		mErrorCount++;
	}
	client->getPolls()->releaseTag(tag);
}

size_t RMessagesProcessor::getReceivedPacketsCount() {
	return mReceivedCount;
}

size_t RMessagesProcessor::getReceivedErrorPacketsCount() {
	return mErrorCount;
}

void RMessagesProcessor::addClient(ClientDetails *state) {
	// nothing to do
}