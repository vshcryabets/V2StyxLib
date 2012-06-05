/*
 * StyxTFlushMessage.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#include "StyxTFlushMessage.h"

StyxTFlushMessage::StyxTFlushMessage(StyxTAG oldtag) :
	StyxMessage(Tflush, NOTAG) {
	mOldTag = oldtag;
}

StyxTFlushMessage::~StyxTFlushMessage() {
	// TODO Auto-generated destructor stub
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTFlushMessage::load(IStyxDataReader *input) {
	mOldTag = input->readUInt16();
}
size_t StyxTFlushMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt16(mOldTag);
}
size_t StyxTFlushMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + sizeof(mOldTag);
}
