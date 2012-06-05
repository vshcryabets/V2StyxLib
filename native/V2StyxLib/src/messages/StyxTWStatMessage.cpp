/*
 * StyxTWStatMessage.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#include "StyxTWStatMessage.h"

StyxTWStatMessage::StyxTWStatMessage(StyxFID fid, StyxStat *stat) :
	StyxMessage(Twstat, NOTAG) {
	mFID = fid;
	mStat = stat;
	mDelete = false;
}

StyxTWStatMessage::~StyxTWStatMessage() {
	if ( mDelete ) {
		delete mStat;
	}
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTWStatMessage::load(IStyxDataReader *input) {
	mFID = input->readUInt32();
    input->readUInt16();
    mStat = new StyxStat(input);
    mDelete = true;
}
size_t StyxTWStatMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
    output->writeUInt32(mFID);
    output->writeUInt16(mStat->getSize());
    mStat->writeBinaryTo(output);
    return getBinarySize();
}
size_t StyxTWStatMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + 4
			+ mStat->getSize();
}
