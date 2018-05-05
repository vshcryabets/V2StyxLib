/*
 * Polls.cpp
 *
 *  Created on: Dec 3, 2016
 *      Author: mrco
 */

#include "utils/Polls.h"

Polls::Polls() {
    mFids = new FIDPoll();
    mTags = new MessageTagPoll();
}

Polls::~Polls() {
	delete mFids;
	delete mTags;
	mMessagesMap.clear();
}

FIDPoll* Polls::getFIDPoll() {
	return mFids;
}

void Polls::releaseFID(StyxTMessageFID* message) {
	mFids->release(message->getFID());
}

void Polls::releaseTag(StyxTAG tag) {
	mMessagesMap.erase(tag);
	mTags->release(tag);
}

StyxTMessage* Polls::getTMessage(StyxTAG tag) {
	std::map<StyxTAG, StyxTMessage*>::iterator it = mMessagesMap.find(tag);
	if (it == mMessagesMap.end()) {
		return NULL;
	}
	return it->second;
}

MessageTagPoll* Polls::getTagPoll() {
	return mTags;
}

void Polls::putTMessage(StyxTAG tag, StyxTMessage* message) {
	mMessagesMap[tag] = message;
}
