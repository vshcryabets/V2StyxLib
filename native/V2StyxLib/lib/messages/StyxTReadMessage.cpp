/*
 * StyxTReadMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxTReadMessage.h"

StyxTReadMessage::StyxTReadMessage(StyxFID fid, uint64_t offset, uint32_t count) :
	StyxTMessageFID(Tread, Rread, fid), mOffset(offset), mCount(count){
}

StyxTReadMessage::~StyxTReadMessage() {
	// TODO Auto-generated destructor stub
}
uint64_t StyxTReadMessage::getOffset() {
	return mOffset;
}
uint32_t StyxTReadMessage::getCount() {
	return mCount;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTReadMessage::load(IStyxDataReader *input) {
	StyxTMessageFID::load(input);
    mOffset = input->readUInt64();
    mCount = input->readUInt32();
}
void StyxTReadMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxTMessageFID::writeToBuffer(output);
    output->writeUInt64(mOffset);
    output->writeUInt32(mCount);
}
size_t StyxTReadMessage::getBinarySize() {
	return StyxTMessageFID::getBinarySize() + sizeof(mOffset) + sizeof(mCount);
}
