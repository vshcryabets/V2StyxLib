/*
 * StyxTReadMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxTReadMessage.h"

StyxTReadMessage::StyxTReadMessage(StyxFID fid, uint64_t offset, uint32_t count) :
	StyxMessage(Tread, NOTAG){
	mFID = fid;
	mOffset = offset;
	mCount = count;
}

StyxTReadMessage::~StyxTReadMessage() {
	// TODO Auto-generated destructor stub
}
StyxFID StyxTReadMessage::getFID() {
	return mFID;
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
    mFID = input->readUInt32();
    mOffset = input->readUInt64();
    mCount = input->readUInt32();
}
size_t StyxTReadMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
    output->writeUInt32(mFID);
    output->writeUInt64(mOffset);
    output->writeUInt32(mCount);
    return getBinarySize();
}
size_t StyxTReadMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + sizeof(mFID) + sizeof(mOffset) + sizeof(mCount);
}
