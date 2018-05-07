/*
 * QueueMessagesProcessor.cpp
 *
 *  Created on: May 5, 2018
 *      Author: vova
 */

#include "handlers/QueueMessagesProcessor.h"

QueueMessagesProcessor::QueueMessagesProcessor(StyxString tag) : mThread(tag), mTag(tag), mErrorPackets(0),
	mHandledPackets(0) {
	mThread.startRunnable(this);
}

QueueMessagesProcessor::~QueueMessagesProcessor() {
	mThread.forceCancel();
}

void QueueMessagesProcessor::postPacket(StyxMessage *message, ClientDetails *target) {
	QueueMessageProcessorPair pair;
	pair.mMessage = message;
	pair.mTransmitter = target;
#warning TODO synchronise this
	mQueue.push(pair);
}

void QueueMessagesProcessor::close() {
	mThread.cancel();
	mThread.join();
}

void* QueueMessagesProcessor::run() {
	while (!mThread.isInterrupted()) {
		QueueMessageProcessorPair pair = mQueue.front();
		mQueue.pop();
		try {
			processPacket(pair.mMessage, pair.mTransmitter);
		} catch (StyxException e) {
			e.printStackTrace();
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

