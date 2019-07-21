/*
 * StyxTOpenMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "messages/StyxTOpenMessage.h"

StyxTOpenMessage::StyxTOpenMessage(StyxFID fid, ModeTypeEnum mode) :
	StyxTMessageFID(Topen, Ropen, fid), mMode(mode) {
}

StyxTOpenMessage::~StyxTOpenMessage() {
	// TODO Auto-generated destructor stub
}
StyxMode StyxTOpenMessage::getMode() {
	return mMode;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTOpenMessage::load(IStyxDataReader *input) {
	StyxTMessageFID::load(input);
    mMode = (StyxMode)input->readUInt8();
}
void StyxTOpenMessage::writeToBuffer(IStyxDataWriter *outputBuffer) {
	StyxTMessageFID::writeToBuffer(outputBuffer);
	outputBuffer->writeUInt8(mMode);
}
size_t StyxTOpenMessage::getBinarySize() {
	return StyxTMessageFID::getBinarySize() + 1;
}
