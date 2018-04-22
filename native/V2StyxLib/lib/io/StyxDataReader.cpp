/*
 * StyxDataReader.cpp
 *
 *  Created on: May 29, 2012
 *      Author: mrco
 */

#include "io/StyxDataReader.h"

StyxDataReader::StyxDataReader(IStyxBuffer *buffer)
	: mBuffer(buffer){
}

StyxDataReader::~StyxDataReader() {
}

uint64_t StyxDataReader::readInteger(const size_t bytes) {
	uint64_t result = 0L;
	int shift = 0;
	const int q = 10;
	uint8_t mDataBuffer[q];
	read(mDataBuffer, 0, bytes);
	for (size_t i=0; i<bytes; i++) {
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
	char* bytes = new char[count];
	read((uint8_t*)bytes, 0, count);
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

