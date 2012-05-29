/*
 * DualStateBuffer.cpp
 *
 *  Created on: May 22, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxByteBufferReadable.h"

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
	mBuffer.limit( mWritePosition < mReadPosition ? mReadPosition : mCapacity );
	mBuffer.position(mWritePosition);
	int readed = channel.read(mBuffer);
	if ( readed > 0 ) {
		mStoredBytes+=readed;
		mWritePosition=mBuffer.position();
	}
	return readed;
}


size_t StyxByteBufferReadable::get(uint8_t[] out, size_t i, size_t length) {
	if ( out == null ) throw new NullPointerException("Out buffer is null");
	if ( mStoredBytes < length ) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
	if ( mReadPosition >= mCapacity ) {
		mReadPosition = 0;
	}
	mBuffer.position(mReadPosition);StyxByteBufferReadable
	int limit = mWritePosition <= mReadPosition ? mCapacity : mWritePosition;
	mBuffer.limit( limit );
	int avaiable = limit-mReadPosition;
	if ( avaiable < length ) {
		// splited block
		// read first part
		mBuffer.get(out, i, avaiable);
		// read second part
		mBuffer.position(0);
		mBuffer.get(out, i+avaiable, length-avaiable);
	} else {
		// single block
		mBuffer.get(out, i, length);
	}
	return length;
}

size_t StyxByteBufferReadable::read(uint8_t[] out, size_t i, size_t length) {
	if ( out == null ) throw new NullPointerException("Out is null");
	if ( mStoredBytes < length ) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
	int res = get(out, i, length);
	mReadPosition=mBuffer.position();
	mStoredBytes-=res;
	return res;
}
