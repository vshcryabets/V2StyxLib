/*
 * FIDPoll.cpp
 *
 */

#include "utils/FIDPoll.h"
#include "messages/base/StyxMessage.h"

FIDPoll::FIDPoll() {
	mLast = 1;
}

FIDPoll::~FIDPoll() {
}

bool FIDPoll::release(StyxFID id) {
    if (id == StyxMessage::NOFID)
        return false;
    return AbstractPoll::release(id);
}

StyxFID FIDPoll::getNext() {
    mLast++;
    if(mLast > FIDPoll::MAXUNINT)
        mLast = 0L;
    return mLast;
}

void FIDPoll::clean() {
	mLast = 0L;
}
