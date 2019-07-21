/*
 * StyxThread.cpp
 *
 */
#include "utils/StyxThread.h"
#include <unistd.h>
#include "utils/Log.h"
#include "exceptions/StyxException.h"

StyxThread::StyxThread(StyxString tag) : mAlive(false), mInterruptFlag(false), mTag(tag) 
{
#warning set thread name from tag
}

StyxThread::~StyxThread() {
    
}

int StyxThread::cancel() {
    mInterruptFlag = true;
    return 0;
    //return pthread_cancel(mThreadId);
}

int StyxThread::start(void* (*startPoint) (void *), void *arg) {
    int result = pthread_create(&mThreadId, NULL, startPoint, arg);
#ifdef THREAD_LIFECYCLE_LOG
    printf("StyxThread::start %s %d\n", mTag.c_str(), result);
#endif
    return result;
}

int StyxThread::startRunnable(IRunnable* runnable) {
    mRunnable = runnable;
    int result = pthread_create(&mThreadId, NULL, &StyxThread::threadIn, this);
#ifdef THREAD_LIFECYCLE_LOG
    printf("StyxThread::startRunnable %s %d\n", mTag.c_str(), result);
#endif
    return result;
}

void* StyxThread::threadIn(void *context) {
    void* result;
    StyxThread* thread = (StyxThread*)context;
#ifdef THREAD_LIFECYCLE_LOG
    printf("StyxThread::threadIn %s start\n", thread->mTag.c_str());
#endif
    thread->mAlive = true;
    if (thread->mRunnable != NULL) {
        try {
            result = thread->mRunnable->run();
        } catch (StyxException error) {
            LogDebug("Got StyxException:\n%d\n%s\n", error.getInternalCode(),
                error.getMessage().c_str());
        }
    }
    thread->mAlive = false;
#ifdef THREAD_LIFECYCLE_LOG
    printf("StyxThread::threadIn %s end\n", thread->mTag.c_str());
#endif
    pthread_exit(result);  
}

bool StyxThread::isInterrupted() {
    return mInterruptFlag;
}

bool StyxThread::isAlive() {
    return mAlive;
}

int StyxThread::tryjoin(size_t timeoutMs) {
    // Linux only
    // struct timespec tm;
    // tm.tv_sec = timeoutMs / 1000;
    // tm.tv_nsec = (timeoutMs - tm.tv_sec * 1000) * 1000000;
    // return pthread_timedjoin_np(mThreadId, NULL, tm);
#ifdef THREAD_LIFECYCLE_LOG
            printf("StyxThread::tryjoin %s\n", mTag.c_str());
#endif
    for (size_t i = 0; i< timeoutMs / 100; i++) {
        ::usleep(100000);
        if ( !mAlive ) {
#ifdef THREAD_LIFECYCLE_LOG
            printf("StyxThread::tryjoin %s ok\n", mTag.c_str());
#endif
            return 0;
        }
    }
#ifdef THREAD_LIFECYCLE_LOG
    printf("StyxThread::tryjoin %s failed\n", mTag.c_str());
#endif
    return -1;
}

int StyxThread::forceCancel() {
    return pthread_cancel(mThreadId);
}

int StyxThread::join(void **value_ptr) {
#ifdef THREAD_LIFECYCLE_LOG
    printf("StyxThread::join %s\n", mTag.c_str());
#endif
    return pthread_join(mThreadId, value_ptr);
}