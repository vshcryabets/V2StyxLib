/*
 * DualStateBuffer.cpp
 *
 *  Created on: May 22, 2012
 *      Author: vschryabets@gmail.com
 */

#include "io/StyxByteBufferReadable.h"
#include "string.h"
#include "stdio.h"

StyxByteBufferReadable::StyxByteBufferReadable(size_t capacity) {
	mWritePosition = 0;
	mReadPosition = 0;
	mStoredBytes = 0;
	mCapacity = capacity;
	mBuffer = new uint8_t[mCapacity];
}

StyxByteBufferReadable::~StyxByteBufferReadable() {
	delete [] mBuffer;
}

size_t StyxByteBufferReadable::remainsToRead() {
	return mStoredBytes;
}

size_t StyxByteBufferReadable::readFromFD(Socket fd) {
	size_t free = mCapacity-mStoredBytes;
	if ( free <= 0 ) return 0;
	if ( mWritePosition >= mCapacity ) {
		mWritePosition = 0;
	}
	size_t count = ( mWritePosition < mReadPosition ? mReadPosition-mWritePosition : mCapacity-mWritePosition );
	size_t position = mWritePosition;
#ifdef WIN32
	int readed = ::recv(fd, (char*)(mBuffer+position), count, 0);
#else
	int readed = ::read(fd, mBuffer+position, count);
#endif
	if ( readed > 0 ) {
		mStoredBytes+=readed;
		mWritePosition+=readed;
	}
	return readed;
}


size_t StyxByteBufferReadable::get(uint8_t* out, size_t i, size_t length) {
	if ( out == NULL ) throw "Out buffer is null";
	if ( mStoredBytes < length ) throw "Too much bytes to read";
	if ( mReadPosition >= mCapacity ) {
		mReadPosition -= mCapacity;
	}
	size_t position = mReadPosition;
	size_t limit = mWritePosition <= mReadPosition ? mCapacity : mWritePosition;
	size_t avaiable = limit-mReadPosition;
	if ( avaiable < length ) {
		// splited block
		// read first part
		memcpy(out+i,mBuffer+position,avaiable);
		// read second part
		memcpy(out+i+avaiable,mBuffer,length-avaiable);
	} else {
		// single block
		memcpy(out+i,mBuffer+position, length);
	}
//	printf("GET: p=%d, l=%d\n", position, limit);
	return length;
}

size_t StyxByteBufferReadable::read(uint8_t *out, size_t i, size_t length) {
	if ( out == NULL ) throw "Out is null";
	if ( mStoredBytes < length ) throw "Too much bytes to read";
//	printf("READ: ps=%d, len=%d\n", mReadPosition, length);
	size_t res = get(out, i, length);
	mReadPosition += res;
	mStoredBytes-=res;
//	printf("READ: ps=%d, stored=%d\n", mReadPosition, mStoredBytes);
	return res;
}

uint32_t StyxByteBufferReadable::getUInt32() {
	uint32_t result = 0;
	get((uint8_t*)&result, 0, 4);
	return result;
}
