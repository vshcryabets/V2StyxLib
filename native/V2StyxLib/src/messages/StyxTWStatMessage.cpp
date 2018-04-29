/*
 * StyxTWStatMessage.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#include "messages/StyxTWStatMessage.h"

StyxTWStatMessage::StyxTWStatMessage(StyxFID fid, StyxStat *stat, bool deleteStat) :
	StyxTMessageFID(Twstat, Rwstat, fid), mStat(stat), mDelete(deleteStat) {
}

StyxTWStatMessage::~StyxTWStatMessage() {
	if ( mDelete && mStat != NULL ) {
		delete mStat;
	}
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTWStatMessage::load(IStyxDataReader *input) {
	StyxTMessageFID::load(input);
    input->readUInt16();
    mStat = new StyxStat(input);
    mDelete = true;
}
void StyxTWStatMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxTMessageFID::writeToBuffer(output);
    output->writeUInt16(mStat->getSize());
    mStat->writeBinaryTo(output);
}
size_t StyxTWStatMessage::getBinarySize() {
	return StyxTMessageFID::getBinarySize()
			+ mStat->getSize();
}
