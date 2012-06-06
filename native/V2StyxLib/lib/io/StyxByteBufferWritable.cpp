/*
 * StyxByteBufferWritable.cpp
 *
 *  Created on: May 31, 2012
 *      Author: vschryabets@gmail.com
 */

#include "io/StyxByteBufferWritable.h"
#include "string.h"

StyxByteBufferWritable::StyxByteBufferWritable(int capacity) {
	mCapacity = capacity;
	mBuffer = new uint8_t[capacity];
	clear();
}

StyxByteBufferWritable::~StyxByteBufferWritable() {
	delete [] mBuffer;
}
size_t StyxByteBufferWritable::getCapacity() {
	return mCapacity;
}
// ==================================================
// IStyxDataWriter methods
// ==================================================
size_t StyxByteBufferWritable::write(const uint8_t* data, size_t offset, size_t count) {
	// TODO check buffer boundaries
	memcpy(mBuffer+mPosition, data+offset, count);
	mPosition+=count;
	return count;
}
void StyxByteBufferWritable::clear() {
	mPosition = 0;
	mLimit = mCapacity;
}
void StyxByteBufferWritable::limit(size_t limit) {
	mLimit = limit;
}

uint8_t* StyxByteBufferWritable::getBuffer() {
	return mBuffer;
}
size_t StyxByteBufferWritable::getPosition() {
	return mPosition;
}
