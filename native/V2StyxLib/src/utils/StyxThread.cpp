/*
 * FIDPoll.cpp
 *
 */

#include "utils/StyxThread.h"
#include <unistd.h>

StyxThread::StyxThread() : mAlive(false), mInterruptFlag(false) {

}

StyxThread::~StyxThread() {
    
}

int StyxThread::cancel() {
    mInterruptFlag = true;
    return 0;
    //return pthread_cancel(mThreadId);
}

int StyxThread::start(void* (*startPoint) (void *), void *arg) {
    return pthread_create(&mThreadId, NULL, startPoint, arg);
}

int StyxThread::startRunnable(IRunnable* runnable) {
    mRunnable = runnable;
    return pthread_create(&mThreadId, NULL, &StyxThread::threadIn, this);
}

void* StyxThread::threadIn(void *context) {
    void* result;
    StyxThread* thread = (StyxThread*)context;
    thread->mAlive = true;
    if (thread->mRunnable != NULL) {
        result = thread->mRunnable->run();
    }
    thread->mAlive = false;
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
    for (size_t i = 0; i< timeoutMs / 100; i++) {
        ::usleep(100);
        if ( !mAlive ) {
            return 0;
        }
    }
    return -1;
}

int StyxThread::forceCancel() {
    return pthread_cancel(mThreadId);
}

int StyxThread::join(void **value_ptr) {
    return pthread_join(mThreadId, value_ptr);
}