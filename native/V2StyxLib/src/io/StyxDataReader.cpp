/*
 * StyxDataReader.cpp
 *
 *  Created on: May 29, 2012
 *      Author: mrco
 */

#include "StyxDataReader.h"

StyxDataReader::StyxDataReader() {
	mDataBuffer = new uint8_t[sDataBufferSize];
}

StyxDataReader::~StyxDataReader() {
	delete [] mDataBuffer;
}

uint64_t StyxDataReader::readInteger(size_t bytes) {
	uint64_t result = 0L;
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

std::string StyxDataReader::readUTFString() {
	size_t count = readUInt16();
	unsigned char* bytes = new char[count];
	read(bytes, 0, count);
	std::string result(bytes, count);
	delete [] bytes;
	return result;
}

uint8_t StyxDataReader::readUInt8() {
	return (short) (readInteger(1)&0XFF);
}

uint16_t StyxDataReader::readUInt16() {
	return (int) (readInteger(2)&0xFFFF);
}

uint32_t StyxDataReader::readUInt32() {
	return (readInteger(4) &0xFFFFFFFF);
}

uint64_t StyxDataReader::readUInt64() {
	return readInteger(8);
}

uint32_t StyxDataReader::getUInt32() {
	return getInteger(4) & 0xFFFFFFFFL;
}
