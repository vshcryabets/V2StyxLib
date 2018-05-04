/*
 * StyxRReadMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxRReadMessage.h"

StyxRReadMessage::StyxRReadMessage(StyxTAG tag, StyxBuffer buffer, size_t length) 
	: StyxMessage(Rread, tag), mData(buffer), mDataLength(length) {
}

StyxRReadMessage::~StyxRReadMessage() {
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRReadMessage::load(IStyxDataReader *input) {
	mDataLength = input->readUInt32();
	mData = StyxBuffer(mDataLength);
	input->read(mData.data(), mDataLength);
}

void StyxRReadMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt32(mDataLength);
	if ( mDataLength > 0 ) {
		output->write(mData.data(), mDataLength);
	}
}

size_t StyxRReadMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + 4
			+ mDataLength;
}
