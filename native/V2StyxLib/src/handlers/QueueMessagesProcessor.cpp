/*
 * QueueMessagesProcessor.cpp
 *
 *  Created on: May 5, 2018
 *      Author: vova
 */
#include <pthread.h>
#include "handlers/QueueMessagesProcessor.h"

QueueMessagesProcessor::QueueMessagesProcessor(StyxString tag) : 
	mThread(tag), mTag(tag), mErrorPackets(0), mHandledPackets(0) {
	pthread_mutex_init(&mQueueMutex, NULL);
	pthread_cond_init(&mQueueCond, NULL);
	mThread.startRunnable(this);
}

QueueMessagesProcessor::~QueueMessagesProcessor() {
	mThread.forceCancel();
	pthread_cond_destroy(&mQueueCond); 
	pthread_mutex_destroy(&mQueueMutex);                                                   
}

void QueueMessagesProcessor::postPacket(StyxMessage *message, ClientDetails *target) {
	QueueMessageProcessorPair pair;
	pair.mMessage = message;
	pair.mTransmitter = target;
	pthread_mutex_lock(&mQueueMutex);
	mQueue.push(pair);
	pthread_cond_signal(&mQueueCond);
	pthread_mutex_unlock(&mQueueMutex);
}

void QueueMessagesProcessor::close() {
	mThread.cancel();
	mThread.join();
}

void* QueueMessagesProcessor::run() {
	struct timespec tm;
	tm.tv_sec = 1;
	tm.tv_nsec = 0;
	while (!mThread.isInterrupted()) {
		pthread_mutex_lock(&mQueueMutex);
		if (mQueue.size() == 0) {
			pthread_cond_wait(&mQueueCond, &mQueueMutex);
		}
		while (mQueue.size() > 0) {
			QueueMessageProcessorPair pair = mQueue.front();
			mQueue.pop();
			try {
				processPacket(pair.mMessage, pair.mTransmitter);
			} catch (StyxException e) {
				e.printStackTrace();
			}
		}
		pthread_mutex_unlock(&mQueueMutex);
	}
	return NULL;
}

void QueueMessagesProcessor::addClient(ClientDetails *state) {
	// nothing to do
}

void QueueMessagesProcessor::removeClient(ClientDetails *state) {
	// nothing to do
}

size_t QueueMessagesProcessor::getReceivedPacketsCount() {
	return mHandledPackets;
}

size_t QueueMessagesProcessor::getReceivedErrorPacketsCount() {
	return mErrorPackets;
}

