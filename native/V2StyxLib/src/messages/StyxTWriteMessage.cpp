/*
 * StyxTWrite.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#include "messages/StyxTWriteMessage.h"

StyxTWriteMessage::StyxTWriteMessage(StyxFID fid, uint64_t offset, uint8_t *data, uint32_t length) :
	StyxTMessageFID(Twrite, Twrite, fid), mOffset(offset), mData(data), mDataLength(length), mDelete(false) {
}

StyxTWriteMessage::~StyxTWriteMessage() {
	if ( mDelete ) {
		delete [] mData;
	}
}
uint8_t* StyxTWriteMessage::getData() {
	return mData;
}
uint64_t StyxTWriteMessage::getOffset() {
	return mOffset;
}
uint32_t StyxTWriteMessage::getCount() {
	return mDataLength;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTWriteMessage::load(IStyxDataReader *input) {
	StyxTMessageFID::load(input);
    mOffset = input->readUInt64();
    mDataLength = (int)input->readUInt32();
    mData = new uint8_t[mDataLength];
    mDelete = true;
    input->read(mData, mDataLength);
}
void StyxTWriteMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxTMessageFID::writeToBuffer(output);
    output->writeUInt64(mOffset);
    output->writeUInt32(mDataLength);
    output->write(mData, mDataLength);
}
size_t StyxTWriteMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + 12
            + mDataLength;
}
