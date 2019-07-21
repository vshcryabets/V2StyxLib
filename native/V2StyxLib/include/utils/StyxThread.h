/*
 * StyxThread.h
 *
 */

#ifndef INCLUDE_UTILS_STYXTHREAD_H_
#define INCLUDE_UTILS_STYXTHREAD_H_

#include "types.h"
#include <pthread.h>

class IRunnable {
public:
	virtual void* run() = 0;
};

class StyxThread {
protected:
	pthread_t mThreadId;
	bool mAlive;
	bool mInterruptFlag;
	IRunnable* mRunnable;
	StyxString mTag;
public:
	StyxThread(StyxString tag);
	~StyxThread();
	int tryjoin(size_t dealyMs);
	bool isAlive();
	bool isInterrupted();
	int cancel(); 
	int forceCancel(); //	pthread_cancel(mAcceptorThread);
	int start(void* (*startPoint) (void *), void *arg);
	int startRunnable(IRunnable* runnale);
	int join(void **value_ptr = NULL); 
	static void* threadIn(void *context);
	StyxString getTag();
};

#endif /* INCLUDE_UTILS_STYXTHREAD_H_ */
