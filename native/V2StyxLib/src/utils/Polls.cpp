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
