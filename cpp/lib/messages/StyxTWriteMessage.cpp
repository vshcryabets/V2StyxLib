/*
 * StyxTWrite.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#include "messages/StyxTWriteMessage.h"

StyxTWriteMessage::StyxTWriteMessage() :
	StyxMessage(Twrite, NOTAG) {
	// TODO Auto-generated constructor stub

}

StyxTWriteMessage::~StyxTWriteMessage() {
	if ( mDelete ) {
		delete [] mData;
	}
}
StyxFID StyxTWriteMessage::getFID() {
	return mFID;
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
//	mOldTag = input->readUInt16();
    mFID = input->readUInt32();
    mOffset = input->readUInt64();
    mDataLength = (int)input->readUInt32();
    mData = new uint8_t[mDataLength];
    mDelete = true;
    input->read(mData, 0, mDataLength);
}
size_t StyxTWriteMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
    output->writeUInt32(mFID);
    output->writeUInt64(mOffset);
    output->writeUInt32(mDataLength);
    output->write(mData, 0, mDataLength);
    return getBinarySize();
}
size_t StyxTWriteMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + 16
            + mDataLength;
}
