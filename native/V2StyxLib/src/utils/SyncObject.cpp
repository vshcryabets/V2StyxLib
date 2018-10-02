/*
 * SyncObject.cpp
 *
 */
#include "utils/SyncObject.h"
#ifdef __MACH__
    #include <mach/clock.h>
    #include <mach/mach.h>
#endif

SyncObject::SyncObject(uint64_t timeout) : mTimeout(timeout) 
{
	pthread_mutex_init(&mMutex, NULL);
	pthread_cond_init(&mCond, NULL);
}

SyncObject::~SyncObject()
{
	pthread_cond_destroy(&mCond); 
	pthread_mutex_destroy(&mMutex);                                                   
}

void SyncObject::waitForNotify(StyxMessage **controlObject) throw(StyxException) 
{
    if (*controlObject != NULL) {
        return;
    }
    pthread_mutex_lock(&mMutex);
    if (*controlObject != NULL) {
        pthread_mutex_unlock(&mMutex);
        return;
    }
    struct timespec tm;
#ifdef __MACH__
    clock_serv_t cclock;
    mach_timespec_t mts;
    host_get_clock_service(mach_host_self(), CALENDAR_CLOCK, &cclock);
    clock_get_time(cclock, &mts);
    mach_port_deallocate(mach_task_self(), cclock);
    tm.tv_sec = mts.tv_sec;
    tm.tv_nsec = mts.tv_nsec;
#else
    clock_gettime(CLOCK_REALTIME, &ts);
#endif
    tm.tv_sec += mTimeout / 1000;
    tm.tv_nsec += (mTimeout - (mTimeout / 1000) * 1000) * 1000000;
    int res = pthread_cond_timedwait(&mCond, &mMutex, &tm);
    pthread_mutex_unlock(&mMutex);
    return;
}

void SyncObject::notifyAll()
{
    pthread_mutex_lock(&mMutex);
    pthread_cond_signal(&mCond);
	pthread_mutex_unlock(&mMutex);
}