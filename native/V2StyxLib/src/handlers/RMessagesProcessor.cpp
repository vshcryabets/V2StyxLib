/*
 * TMessagesProcessor.cpp
 *
 *  Created on: Apr 13, 2018
 *      Author: vshcryabets@gmail.com
 */
#include "handlers/RMessagesProcessor.h"

RMessagesProcessor::RMessagesProcessor(StyxString tag)
	: mTag(tag) {
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
	std::map<StyxTAG, StyxTMessage*>* clientMessagesMap = client->getPolls()->getMessagesMap();
	std::map<StyxTAG, StyxTMessage*>::iterator it = clientMessagesMap->find(tag);
	if (it == clientMessagesMap->end()) {
		// we didn't send T message with such tag, so ignore this R message
		throw StyxException("Got (%s) unknown R message from client %s\n",
				mTag.c_str(),
				client->toString().c_str());
	}
	StyxTMessage* tMessage = it->second;
	// TODO i'm not sure that this is proper place for that logic
	if (tMessage->getType() == MessageTypeEnum::Tclunk ||
			tMessage->getType() == MessageTypeEnum::Tremove) {
		client->getPolls()->releaseFID((StyxTMessageFID*) tMessage);
	}
	try {
		tMessage->setAnswer(message);
	} catch (StyxException e) {
		e.printStackTrace();
	}
	if (message->getType() == MessageTypeEnum::Rerror) {
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