/*
 * StyxROpenMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "StyxROpenMessage.h"

StyxROpenMessage::StyxROpenMessage(StyxTAG tag, StyxQID* qid, size_t iounit) :
	StyxMessage(Ropen, tag){
	mQID = qid;
	mIOUnit = iounit;
	mDelete = false;
}

StyxROpenMessage::~StyxROpenMessage() {
	if ( mDelete ) {
		delete mQID;
	}
}
// =======================================================
// Virtual methods
// =======================================================
void StyxROpenMessage::load(IStyxDataReader *input) {
    mQID = new StyxQID(input);
    mIOUnit = input->readUInt32();
    mDelete = true;
}
size_t StyxROpenMessage::writeToBuffer(IStyxDataWriter *output) {
    StyxMessage::writeToBuffer(output);
	mQID->writeBinaryTo(output);
	output->writeUInt32(mIOUnit);
	return getBinarySize();
}
size_t StyxROpenMessage::getBinarySize() {
	return StyxMessage::getBinarySize()
		+ StyxQID::CONTENT_SIZE + 4;
}
