/*
 * Mutex.h
 *
 */

#ifndef INCLUDE_UTILS_MUTEX_H_
#define INCLUDE_UTILS_MUTEX_H_

#include <pthread.h>
#include <stdint.h>

class Mutex {
protected:
	pthread_mutex_t mMutex;
public:
	Mutex();
	~Mutex();

	void lock();
	void unlock();
	pthread_mutex_t* getPThreadMutex();
};

class Condition : public Mutex
{
protected:
	pthread_cond_t mCond;
public:
	Condition();
	~Condition();
	// void wait();
	void notifyNoLock();
	void notifyWithLock();
	void waitNoLock(uint64_t timeoutMs);
};

class MutexBlock {
private:
	Mutex* mMutex;
public:
	MutexBlock(Mutex* mutex);
	~MutexBlock();
};

#endif /* INCLUDE_UTILS_MUTEX_H_ */
