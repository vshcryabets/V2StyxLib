/*
 * StyxByteBufferWritable.cpp
 *
 *  Created on: May 31, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxByteBufferWritable.h"

StyxByteBufferWritable::StyxByteBufferWritable(int capacity) {
	mCapacity = capacity;
	mBuffer = new uint8_t[capacity];
	clear();
}

StyxByteBufferWritable::~StyxByteBufferWritable() {
	delete [] mBuffer;
}
// ==================================================
// IStyxDataWriter methods
// ==================================================
size_t StyxByteBufferWritable::write(uint8_t* data, size_t offset, size_t count) {
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
