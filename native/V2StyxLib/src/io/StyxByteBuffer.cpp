/*
 * StyxByteBuffer.cpp
 *
 *  Created on: May 27, 2012
 *      Author: mrco
 */

#include "StyxByteBuffer.h"

StyxByteBuffer::StyxByteBuffer(const char* buffer) :
mBuffer(buffer) {

}

StyxByteBuffer::~StyxByteBuffer() {
}

long getInteger(int bytes) {
	int position = mBuffer.position();
	long result = readInteger(bytes);
	mBuffer.position(position);
	return result;
}

/**
 * Reset position & limit
 */
void clear() {
	mBuffer.position(0);
	mBuffer.limit(mBuffer.capacity());
}
void limit(int packetSize) {
	mBuffer.limit(packetSize);
}

/**
 *
 * @return Byte buffer
 */
ByteBuffer getBuffer() {
	return mBuffer;
}

void write(byte[] data) {
	mBuffer.put(data);
}

void write(byte[] data, int offset, int count) {
	mBuffer.put(data, offset, count);
}

int read(byte[] mData, int offset, int count) {
	mBuffer.get(mData, offset, count);
	return count;
}

/**
 * Seek to specified position in buffer
 * @param offset
 */
void setPosition(int offset) {
	mBuffer.position(offset);
}
