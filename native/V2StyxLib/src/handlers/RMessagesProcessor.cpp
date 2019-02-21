/*
 * TMessagesProcessor.cpp
 *
 *  Created on: Apr 13, 2018
 *      Author: vshcryabets@gmail.com
 */
#include "handlers/RMessagesProcessor.h"

RMessagesProcessor::RMessagesProcessor(StyxString tag)
	: QueueMessagesProcessor(tag) {
}

RMessagesProcessor::~RMessagesProcessor() {
	// TODO Auto-generated destructor stub
}

void RMessagesProcessor::processPacket(StyxMessage *message, ClientDetails *client) throw(StyxException) {
	mHandledPackets++;
	StyxTAG tag = message->getTag();
	StyxTMessage* tMessage = client->findTMessageAndRelease(tag);
	if (tMessage == NULL) {
		// we didn't send T message with such tag, so ignore this R message
		throw StyxException("RMP(%s) got unknown R message from server %s",
				mTag.c_str(),
				client->toString().c_str());
	}
	#warning im not sure that this is proper place for that logic
	if (tMessage->getType() == Tclunk ||
			tMessage->getType() == Tremove) {
		client->releaseFID((StyxTMessageFID*) tMessage);
	}
	try {
		tMessage->setAnswer(message);
	} catch (StyxException e) {
		e.printStackTrace();
	}
	if (message->getType() == Rerror) {
		mErrorPackets++;
	}
}

