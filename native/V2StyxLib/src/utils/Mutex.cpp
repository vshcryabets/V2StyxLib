/*
 * Mutex.cpp
 *
 *  Created on: Apr 5, 2018
 *      Author: mrco
 */

#include "utils/Mutex.h"
#ifdef __MACH__
    #include <mach/clock.h>
    #include <mach/mach.h>
#endif

MutexBlock::MutexBlock(Mutex* mutex) : mMutex(mutex) {
	mMutex->lock();
}

MutexBlock::~MutexBlock() {
	mMutex->unlock();
}

Mutex::Mutex() {
	pthread_mutex_init(&mMutex, NULL);
}

Mutex::~Mutex() {
	pthread_mutex_destroy(&mMutex);
}

void Mutex::lock() {
	pthread_mutex_lock(&mMutex);
}

void Mutex::unlock() {
	pthread_mutex_unlock(&mMutex);
}

pthread_mutex_t* Mutex::getPThreadMutex()
{
	return &mMutex;
}

Condition::Condition() : Mutex()
{
	pthread_cond_init(&mCond, NULL);
}

Condition::~Condition()
{
	pthread_cond_destroy(&mCond); 
}

void Condition::notifyWithLock()
{
	pthread_mutex_lock(&mMutex);
	pthread_cond_signal(&mCond);
	pthread_mutex_unlock(&mMutex);
}

void Condition::notifyNoLock()
{
	pthread_cond_signal(&mCond);
}

void Condition::waitNoLock(uint64_t timeout)
{
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
	clock_gettime(CLOCK_REALTIME, &tm);
#endif
	tm.tv_sec += timeout / 1000;
	tm.tv_nsec += (timeout - (timeout / 1000) * 1000) * 1000000;
	int res = pthread_cond_timedwait(&mCond, &mMutex, &tm);
}