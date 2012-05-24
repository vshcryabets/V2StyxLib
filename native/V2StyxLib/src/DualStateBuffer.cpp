/*
 * DualStateBuffer.cpp
 *
 *  Created on: May 22, 2012
 *      Author: mrco
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
	// TODO Auto-generated destructor stub
}

size_t DualStateBuffer::remainsToRead() {
	return mStoredBytes;
}

uint32_t DualStateBuffer::getInteger(int bytes) {
	// TODO this method will work wrong at the buffer end
	if ( bytes < sDataBufferSize ) throw "To much bytes to read";
	long result = 0L;
	int shift = 0;
	int readed = get(mDataBuffer, 0, bytes);
	if ( readed != bytes ) throw "Can't read bytes";
	for (int i=0; i<bytes; i++) {
		long b = (mDataBuffer[i]&0xFF);
		if (shift > 0)
			b <<= shift;
		shift += 8;
		result |= b;
	}
	return result;
}

size_t DualStateBuffer::get(int8_t *out, size_t offset, size_t length) {
    if ( out == NULL ) throw "Out buffer is null";
    if ( mStoredBytes < length ) throw "Too much bytes to read";
    if ( mReadPosition >= mCapacity ) {
        mReadPosition = 0;
    }
//    mBuffer.position(mReadPosition);
    int limit = mWritePosition <= mReadPosition ? mCapacity : mWritePosition;
//    mBuffer.limit( limit );
    int avaiable = limit-mReadPosition;
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
size_t DualStateBuffer::read(int8_t* data, size_t offset, size_t count) {
    if ( data == NULL ) throw "Out is null";
    if ( mStoredBytes < count ) throw "Too much bytes to read";
    size_t res = get(data, offset, count);
    mReadPosition += res;
    mStoredBytes -= res;
    return res;
}

size_t DualStateBuffer::readFromFD(Socket fd) {
    size_t free = mCapacity-mStoredBytes;
    if ( free <= 0 ) return 0;
    if ( mWritePosition >= mCapacity ) {
        mWritePosition = 0;
    }
//    mBuffer.limit( mWritePosition < mReadPosition ? mReadPosition : mCapacity );
//    mBuffer.position(mWritePosition);
//    size_t readed = ::read(fd, mDataBuffer+mWritePosition, )
//    		channel.read(mBuffer);
//    if ( readed > 0 ) {
//        mStoredBytes+=readed;
//        mWritePosition=mBuffer.position();
//    }
//    return readed;
    return -1;
}
