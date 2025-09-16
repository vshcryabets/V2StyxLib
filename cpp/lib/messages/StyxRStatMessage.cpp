/*
 * StyxRStatMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxRStatMessage.h"

StyxRStatMessage::StyxRStatMessage(StyxTAG tag, StyxStat *stat) :
	StyxMessage(Rstat, tag) {
	mStat = stat;
}

StyxRStatMessage::~StyxRStatMessage() {
	// TODO Auto-generated destructor stub
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRStatMessage::load(IStyxDataReader *input) {
    input->readUInt16();
    mStat = new StyxStat(input);
}
size_t StyxRStatMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
    output->writeUInt16(mStat->getSize());
    mStat->writeBinaryTo(output);
    return getBinarySize();
}
size_t StyxRStatMessage::getBinarySize() {
    return StyxMessage::getBinarySize()+
            + 2 + mStat->getSize();
}
