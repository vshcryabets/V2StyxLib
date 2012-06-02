/*
 * StyxRVersionMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxRAttachMessage.h"

StyxRAttachMessage::StyxRAttachMessage(int tag, StyxQID *qid)
: StyxMessage( Rattach, tag ) {
	mQID = qid;
}

StyxRAttachMessage::~StyxRAttachMessage() {
	// TODO Auto-generated destructor stub
}

void StyxRAttachMessage::load(IStyxDataReader *buffer) {
	mQID = new StyxQID(buffer);
}

size_t StyxRAttachMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	mQID->writeBinaryTo(output);
	return getBinarySize();
}

size_t StyxRAttachMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + StyxQID::CONTENT_SIZE;
}
