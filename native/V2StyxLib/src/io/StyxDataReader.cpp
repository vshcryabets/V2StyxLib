/*
 * StyxDataReader.cpp
 *
 *  Created on: May 29, 2012
 *      Author: mrco
 */

#include "io/StyxDataReader.h"

StyxDataReader::StyxDataReader(IStyxBuffer *buffer)
	: mBuffer(buffer) {
	mInternalBuffer = new uint8_t[getInternalBufferSize()];
}

StyxDataReader::~StyxDataReader() {
	if (mInternalBuffer != NULL) {
		delete mInternalBuffer;
		mInternalBuffer = NULL;
	}
}

uint64_t StyxDataReader::readInteger(const size_t bytes) {
	uint64_t result = getInteger(bytes);
    mBuffer->moveReadPointerBy(bytes);
    return result;
}

uint64_t StyxDataReader::getInteger(const size_t bytes) {
    if ( bytes > getInternalBufferSize() ) {
    		throw StyxException("Too much bytes to read");
    }
    uint64_t result = 0L;
    int shift = 0;
    int readed = mBuffer->get(mInternalBuffer, bytes);
    if ( readed != bytes ) {
    		throw StyxException("Can't read bytes");
    }
    for (size_t i = 0; i < bytes; i++) {
        long b = (mInternalBuffer[i] & 0xFF);
        if (shift > 0)
            b <<= shift;
        shift += 8;
        result |= b;
    }
    return result;

}

StyxString StyxDataReader::readUTFString() {
	size_t count = readUInt16();
	char* bytes = new char[count];
	read((uint8_t*)bytes, count);
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

size_t StyxDataReader::read(uint8_t *data, size_t dataLength) {
	return mBuffer->read(data, dataLength);
}

uint32_t StyxDataReader::getUInt32() {
	return getInteger(4) & 0xFFFFFFFFL;
}

size_t StyxDataReader::getInternalBufferSize() {
	return sDataBufferSize;
}

