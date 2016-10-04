/*
 * StyxROpenMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "messages/StyxROpenMessage.h"

StyxROpenMessage::StyxROpenMessage(StyxTAG tag, StyxQID* qid, size_t iounit) :
	StyxRSingleQIDMessage(Ropen, tag, qid){
	mIOUnit = iounit;
}

StyxROpenMessage::~StyxROpenMessage() {
}
// =======================================================
// Virtual methods
// =======================================================
void StyxROpenMessage::load(IStyxDataReader *input) {
    mIOUnit = input->readUInt32();
}

void StyxROpenMessage::writeToBuffer(IStyxDataWriter *output) {
	StyxRSingleQIDMessage::writeToBuffer(output);
	output->writeUInt32(mIOUnit);
}

size_t StyxROpenMessage::getBinarySize() {
	return StyxRSingleQIDMessage::getBinarySize() + 4;
}
