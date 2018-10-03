/*
 * QueueMessagesProcessor.cpp
 *
 *  Created on: May 5, 2018
 *      Author: vova
 */
#include "handlers/QueueMessagesProcessor.h"

QueueMessagesProcessor::QueueMessagesProcessor(StyxString tag) : 
	mCondition(), mThread(tag), mTag(tag), mErrorPackets(0), mHandledPackets(0) {
	printf("QueueMessagesProcessor %s init\n", mTag.c_str());
	mThread.startRunnable(this);
}

QueueMessagesProcessor::~QueueMessagesProcessor() {
	mThread.forceCancel();
	printf("QueueMessagesProcessor destroy\n");
}

void QueueMessagesProcessor::postPacket(StyxMessage *message, ClientDetails *target) {
	QueueMessageProcessorPair pair;
	pair.mMessage = message;
	pair.mTransmitter = target;
#ifdef LOG_MESSAGES_QUEUE
	printf("QueueMessagesProcessor %s post %s\n", mTag.c_str(), message->toString().c_str());
#endif
	MutexBlock block(&mCondition);
	mQueue.push(pair);
	mCondition.notifyNoLock();
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
		MutexBlock lock(&mCondition);
		if (mQueue.size() == 0) {
			#warning magic number
			mCondition.waitNoLock(1000);
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

