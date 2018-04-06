/*
 * FIDPoll.h
 *
 */

#ifndef INCLUDE_UTILS_FIDPOLL_H_
#define INCLUDE_UTILS_FIDPOLL_H_

#include "types.h"
#include <set>
#include "utils/Mutex.h"

template <class T>
class AbstractPoll {
protected:
    T mLast;
    std::set<T> mAvailable;
    Mutex mMutex;
public:
	AbstractPoll();
	virtual ~AbstractPoll();

	virtual T getFreeItem();
	virtual bool release(T id);
	virtual void clean();
	virtual T getNext() = 0;
};

class FIDPoll : public AbstractPoll<StyxFID> {
protected:
	static const uint32_t MAXUNINT = 0xFFFFFFFF;
public:
	FIDPoll();
	virtual ~FIDPoll();
	virtual bool release(StyxFID id);
	virtual StyxFID getNext();
	virtual void clean();
};

class MessageTagPoll : public AbstractPoll<StyxTAG> {
protected:
	static const uint32_t MAXUSHORT = 0xFFFF;
public:
	MessageTagPoll();
	virtual ~MessageTagPoll();
	virtual StyxTAG getNext();
	virtual bool release(StyxTAG id);
	virtual void clean();
};


#endif /* INCLUDE_UTILS_FIDPOLL_H_ */
