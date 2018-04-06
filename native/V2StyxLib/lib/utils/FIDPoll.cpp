/*
 * FIDPoll.cpp
 *
 */

#include <sstream>
#include "utils/FIDPoll.h"
#include "messages/base/StyxMessage.h"
#include "exceptions/StyxException.h"

template <class T>
AbstractPoll<T>::AbstractPoll() {
}

template <class T>
AbstractPoll<T>::~AbstractPoll() {
}

template <class T>
T AbstractPoll<T>::getFreeItem() {
	MutexBlock lock(&mMutex);
    if (!mAvailable.empty()) {
        T result = *mAvailable.begin();
        mAvailable.erase(mAvailable.begin());
        return result;
    }
    mLast = getNext();
    return mLast;
}

template <class T>
bool AbstractPoll<T>::release(T id) {
	MutexBlock lock(&mMutex);
    if ( mAvailable.find(id) != mAvailable.end()) {
		std::stringstream stream;
		stream << "Something goes wrong, this item already has been released " << id;
        throw StyxException(stream.str());
    }
    return mAvailable.insert(id).second;
}

template <class T>
void AbstractPoll<T>::clean() {
	mAvailable.clear();
	mLast = 0;
}


FIDPoll::FIDPoll() {
	mLast = 0;
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

MessageTagPoll::MessageTagPoll() {
	mLast = 0;
}

MessageTagPoll::~MessageTagPoll() {
}

bool MessageTagPoll::release(StyxTAG id) {
    if (id == StyxMessage::NOTAG)
        return false;
    return AbstractPoll::release(id);
}

StyxTAG MessageTagPoll::getNext() {
    mLast++;
    if(mLast > MessageTagPoll::MAXUSHORT)
        mLast = 0L;
    return mLast;
}

void MessageTagPoll::clean() {
	mLast = 0L;
}
