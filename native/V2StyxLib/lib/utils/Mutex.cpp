/*
 * Mutex.cpp
 *
 *  Created on: Apr 5, 2018
 *      Author: mrco
 */

#include "utils/Mutex.h"

MutexBlock::MutexBlock(Mutex* mutex) {
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

