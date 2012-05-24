/*
 * StyxBufferOperations.cpp
 *
 *  Created on: May 22, 2012
 *      Author: mrco
 */

#include "StyxBufferOperations.h"

StyxBufferOperations::StyxBufferOperations() {
	mDataBuffer = new int8_t[sDataBufferSize];
}

StyxBufferOperations::~StyxBufferOperations() {
	delete [] mDataBuffer;
}

uint32_t StyxBufferOperations::getUInt32() {return getInteger(4);}
uint32_t StyxBufferOperations::readUInt32() {return readInteger(4);}
uint16_t StyxBufferOperations::readUInt16() {return readInteger(2);}
uint8_t StyxBufferOperations::readUInt8() {return readInteger(1);}

uint64_t StyxBufferOperations::readInteger(int bytes) {
	long result = 0L;
	int shift = 0;
	read(mDataBuffer, 0, bytes);
	for (int i=0; i<bytes; i++) {
		long b = (mDataBuffer[i]&0xFF);
		if (shift > 0)
			b <<= shift;
		shift += 8;
		result |= b;
	}
	return result;
}

std::string StyxBufferOperations::readUTF() {
	size_t count = readUInt16();
	int8_t* bytes = new int8_t[count];
	read(bytes, 0, count);
//	std::string result = bytes;
	delete [] bytes;
	return result;
}
