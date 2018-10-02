/*
 * DualStateBuffer.cpp
 *
 *  Created on: May 22, 2012
 *      Author: vschryabets@gmail.com
 */

#include "io/StyxByteBufferReadable.h"
#include "string.h"
#include "stdio.h"
#include <unistd.h>

StyxByteBufferReadable::StyxByteBufferReadable(size_t capacity) {
	mWritePosition = 0;
	mReadPosition = 0;
	mStoredBytes = 0;
	mCapacity = capacity;
	mBuffer = new uint8_t[mCapacity];
	mCurrentLimit = 0;
}

StyxByteBufferReadable::~StyxByteBufferReadable() {
	delete [] mBuffer;
}

size_t StyxByteBufferReadable::remainsToRead() {
	return mStoredBytes;
}

ssize_t StyxByteBufferReadable::readFromChannel(Socket fd) throw(StyxException) {
	size_t free = updateBufferLimits();
	size_t count = ( mWritePosition < mReadPosition ? mReadPosition - mWritePosition : mCapacity - mWritePosition );
	size_t position = mWritePosition;
	ssize_t readed;
#ifdef WIN32
	readed = ::recv(fd, (char*)(mBuffer+position), count, 0);
#else
	readed = ::read(fd, mBuffer + position, count);
	printf("StyxByteBufferReadable::readFromChannel %d\n", readed);
	if (readed > 0) {
		for (size_t i =0; i < readed; i++) {
			printf("%02x ", mBuffer[position + i]);
		}
		printf("\n");
	}
#endif
	if ( readed > 0 ) {
		mStoredBytes += readed;
		mWritePosition += readed;
	}
	return readed;
}

size_t StyxByteBufferReadable::write(uint8_t *buffer, size_t length) {
	size_t free = updateBufferLimits();
	if ( free <= 0 ) {
		// not enough free space
		return 0;
	}
	if ( length > free ) {
		length = free;
	}
	int part1size = mCapacity-mWritePosition;
	if ( part1size > length ) {
		part1size = length;
	}
	int part2size = length-part1size;
	if ( part1size > 0 ) {
		memcpy(mBuffer + mWritePosition, buffer, part1size);
		moveWritePointer(part1size);
	}
	if ( part2size > 0 ) {
		// rewind write pointer to start of buffer
		updateBufferLimits();
		memcpy(mBuffer + mWritePosition, buffer + part1size, part2size);
		moveWritePointer(part2size);
	}
	return length;
}

void StyxByteBufferReadable::moveWritePointer(size_t read) {
	if ( read > 0 ) {
		mStoredBytes += read;
		mWritePosition += read;
	}
}

uint8_t* StyxByteBufferReadable::getBuffer() {
	return mBuffer;
}

void StyxByteBufferReadable::clear() {
	mWritePosition = 0;
	mReadPosition = 0;
	mStoredBytes = 0;
	mCurrentLimit = mCapacity;
}

size_t StyxByteBufferReadable::updateBufferLimits() {
	size_t free = mCapacity - mStoredBytes;
	if ( free <= 0 ) return 0;
	if ( mWritePosition >= mCapacity ) {
		mWritePosition = 0;
	}
	return free;
}

size_t StyxByteBufferReadable::get(uint8_t* out, size_t length) {
	if ( out == NULL ) {
		throw "Out buffer is null";
	}
	if ( mStoredBytes < length ) {
		throw "Too much bytes to read";
	}
	if ( mReadPosition >= mCapacity ) {
		mReadPosition -= mCapacity;
	}
	size_t position = mReadPosition;
	size_t limit = mWritePosition <= mReadPosition ? mCapacity : mWritePosition;
	size_t avaiable = limit-mReadPosition;
	if ( avaiable < length ) {
		// split block
		// read first part
		memcpy(out,mBuffer+position,avaiable);
		// read second part
		memcpy(out+avaiable,mBuffer,length-avaiable);
	} else {
		// single block
		memcpy(out,mBuffer+position, length);
	}
//	printf("GET: p=%d, l=%d\n", position, limit);
	return length;
}

void StyxByteBufferReadable::moveReadPointerBy(size_t bytes) {
	mReadPosition += bytes;
	mStoredBytes -= bytes;
	while ( mReadPosition > mCapacity ) {
		mReadPosition -= mCapacity;
	}
}

size_t StyxByteBufferReadable::read(uint8_t *out, size_t length) {
	if ( out == NULL ) {
		throw "Out is null";
	}
	if ( mStoredBytes < length ) {
		throw "Too much bytes to read";
	}
//	printf("READ: ps=%d, len=%d\n", mReadPosition, length);
	size_t res = get(out, length);
	mReadPosition += res;
	mStoredBytes-=res;
//	printf("READ: ps=%d, stored=%d\n", mReadPosition, mStoredBytes);
	return res;
}
