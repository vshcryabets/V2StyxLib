/*
 * DualStateBuffer.cpp
 *
 *  Created on: May 22, 2012
 *      Author: vschryabets@gmail.com
 */

#include "DualStateBuffer.h"
#include <string.h>

DualStateBuffer::DualStateBuffer(size_t size) {
	mDataBuffer = new int8_t[size];
	mCapacity = size;
	mReadPosition = 0;
	mStoredBytes = 0;
	mWritePosition = 0;
}

DualStateBuffer::~DualStateBuffer() {
	delete [] mDataBuffer;
}

size_t DualStateBuffer::remainsToRead() {
	return mStoredBytes;
}

uint64_t DualStateBuffer::getInteger(size_t bytes) {
	if ( bytes > sDataBufferSize ) throw "To much bytes to read";
	uint64_t result = 0L;
	size_t shift = 0;
	size_t readed = get(mDataBuffer, 0, bytes);
	if ( readed != bytes ) throw "Can't read bytes";
	for (size_t i=0; i<bytes; i++) {
		uint64_t b = (mDataBuffer[i]&0xFF);
		if (shift > 0)
			b <<= shift;
		shift += 8;
		result |= b;
	}
	return result;
}

size_t DualStateBuffer::get(void *out, size_t offset, size_t length) {
	if ( out == NULL ) throw "Out buffer is null";
	if ( mStoredBytes < length ) throw "Too much bytes to read";
	if ( mReadPosition >= mCapacity ) {
		mReadPosition = 0;
	}
	size_t limit = mWritePosition <= mReadPosition ? mCapacity : mWritePosition;
	size_t avaiable = limit-mReadPosition;
	if ( avaiable < length ) {
		// splited block
		// read first part
		memcpy(out+offset, mDataBuffer+mReadPosition, avaiable);
		// read second part
		memcpy(out+offset+avaiable, mDataBuffer, length-avaiable);
	} else {
		// single block
		memcpy(out+offset, mDataBuffer+mReadPosition, length);
	}
	return length;
}

void DualStateBuffer::clear() {
	// TODO not implemented
}
void DualStateBuffer::limit(size_t value) {
	// TODO not implemented
}
void DualStateBuffer::write(int8_t* data) {
	// TODO not implemented
}
void DualStateBuffer::write(int8_t* data, size_t offset, size_t count) {
	// TODO not implemented
}
size_t DualStateBuffer::read(void* data, size_t offset, size_t count) {
	if ( data == NULL ) throw "Out is null";
	if ( mStoredBytes < count ) throw "Too much bytes to read";
	size_t res = get(data, offset, count);
	mReadPosition += res;
	mStoredBytes -= res;
	return res;
}

int DualStateBuffer::readFromFD(Socket fd) {
	size_t free = mCapacity-mStoredBytes;
	if ( free <= 0 ) return 0;
	if ( mWritePosition >= mCapacity ) {
		mWritePosition = 0;
	}
	size_t limit = ( mWritePosition < mReadPosition ? mReadPosition : mCapacity );
	int readed = ::read(fd, mDataBuffer+mWritePosition, limit);
	if ( readed > 0 ) {
		mStoredBytes+=readed;
		mWritePosition+=readed;
	}
	return readed;
}
