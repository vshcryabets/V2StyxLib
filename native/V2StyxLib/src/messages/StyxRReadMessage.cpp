/*
 * StyxRReadMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxRReadMessage.h"

StyxRReadMessage::StyxRReadMessage(StyxTAG tag, uint8_t *data, size_t length) :
StyxMessage(Rread, tag){
	mData = data;
	mDataLength = length;
	mDelete = true; // TODO !!!!!! very bad trick to delete data buffer allocated in ClientState::processRead
}

StyxRReadMessage::~StyxRReadMessage() {
	if ( mDelete ) {
		delete [] mData;
	}
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRReadMessage::load(IStyxDataReader *input) {
	mDataLength = input->readUInt32();
	mData = new uint8_t[mDataLength];
	mDelete = true;
	input->read(mData, 0, mDataLength);
}
size_t StyxRReadMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt32(mDataLength);
	if ( mDataLength > 0 ) {
		output->write(mData, 0, mDataLength);
	}
	return getBinarySize();
}
size_t StyxRReadMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + 4
			+ mDataLength;
}
