/*
 * StyxRWriteMessage.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#include "messages/StyxRWriteMessage.h"

StyxRWriteMessage::StyxRWriteMessage(StyxTAG tag, size_t writed) :
	StyxMessage(Rwrite, tag ) {
	mCount = writed;
}

StyxRWriteMessage::~StyxRWriteMessage() {
	// TODO Auto-generated destructor stub
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRWriteMessage::load(IStyxDataReader *input) {
	mCount = input->readUInt32();
}
void StyxRWriteMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt32(mCount);
}
size_t StyxRWriteMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + 4;
}
