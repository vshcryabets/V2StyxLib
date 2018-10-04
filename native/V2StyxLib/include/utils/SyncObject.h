/*
 * SyncObject.h
 *
 */

#ifndef INCLUDE_UTILS_SYNCOBJECT_H_
#define INCLUDE_UTILS_SYNCOBJECT_H_
#include <stdint.h>
#include "exceptions/StyxException.h"
#include <pthread.h>
#include "messages/base/StyxMessage.h"
#include "utils/Mutex.h"

#warning looks like we can remove this class.
class SyncObject {
private:
	uint64_t mTimeout;
	Condition mCond;
public:
	SyncObject(uint64_t timeout);
	~SyncObject();

	void notifyAll();
    void waitForNotify(StyxMessage **controlObject) throw(StyxException);
    uint64_t getTimeout();
    void setTimeout(uint64_t timeout);
};

#endif /* INCLUDE_UTILS_SYNCOBJECT_H_ */