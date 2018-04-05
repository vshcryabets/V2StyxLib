/*
 * Mutex.h
 *
 */

#ifndef INCLUDE_UTILS_MUTEX_H_
#define INCLUDE_UTILS_MUTEX_H_

#include <pthread.h>

class Mutex {
protected:
	pthread_mutex_t mMutex;
public:
	Mutex();
	~Mutex();

	void lock();
	void unlock();
};

class MutexBlock {
private:
	Mutex* mMutex;
public:
	MutexBlock(Mutex* mutex);
	~MutexBlock();
};

#endif /* INCLUDE_UTILS_MUTEX_H_ */
