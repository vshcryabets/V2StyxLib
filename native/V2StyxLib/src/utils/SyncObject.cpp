/*
 * SyncObject.cpp
 *
 */
#include "utils/SyncObject.h"

SyncObject::SyncObject(uint64_t timeout) : mTimeout(timeout), mCond() 
{	
}

SyncObject::~SyncObject()
{	
}

void SyncObject::waitForNotify(StyxMessage **controlObject) throw(StyxException) 
{
    if (*controlObject != NULL) {
        return;
    }
    {
        MutexBlock lock(&mCond);
        if (*controlObject != NULL) {
            return;
        }
        mCond.waitNoLock(mTimeout);
    }
    return;
}

void SyncObject::notifyAll()
{
    mCond.notifyWithLock();
}