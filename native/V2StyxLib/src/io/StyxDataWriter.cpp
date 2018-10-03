/*
 * StyxDataWriter.cpp
 *
 *  Created on: May 31, 2012
 *      Author: vschryabets@gmail.com
 */

#include "io/StyxDataWriter.h"

StyxDataWriter::StyxDataWriter(StyxBuffer* buffer)
	: mBuffer(buffer), mMaxWritePosition(0) {
}

StyxDataWriter::~StyxDataWriter() {
	// TODO Auto-generated destructor stub
}

void StyxDataWriter::writeUInt8(uint8_t val) {
	write(&val, 1);
}
void StyxDataWriter::writeUInt16(uint16_t val) {
	uint8_t temp[] = { val&0xFF, (val>>8)&0xFF};
	write(temp, 2);
}
void StyxDataWriter::writeUInt32(uint32_t val) {
	uint8_t temp[] = { val&0xFF, (val>>8)&0xFF, (val>>16)&0xFF,  (val>>24)&0xFF };
	write(temp, 4);
}
void StyxDataWriter::writeUInt64(uint64_t val) {
	uint8_t temp[] = { val&0xFF, (val>>8)&0xFF, (val>>16)&0xFF,  (val>>24)&0xFF,
			(val>>32)&0xFF, (val>>40)&0xFF, (val>>48)&0xFF,  (val>>56)&0xFF};
	write(temp, 8);
}
void StyxDataWriter::writeUTFString(StyxString string) {
	writeUInt16(string.length());
	write((const uint8_t*)string.c_str(), string.length());
}

size_t StyxDataWriter::getPosition() {
	return mBuffer->size();
}

void StyxDataWriter::clear() {
	mBuffer->clear();
	// TODO probably clear will resize buffer?
}

void StyxDataWriter::limit(size_t limit) {
	mMaxWritePosition = limit;
}

size_t StyxDataWriter::write(const uint8_t* data, size_t count) throw(StyxException){
#warning optimize this logic
	//  writeposition maxwriteposition
	for (size_t i = 0; i < count; i++) {
		if (mBuffer->size() == mMaxWritePosition) {
			throw StyxException("Buffer overflow max=%d current=%d", mMaxWritePosition, mBuffer->size());
		}
		mBuffer->push_back(data[i]);
	}
	return count;
}