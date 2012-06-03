/*
 * StyxTOpenMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "StyxTOpenMessage.h"

StyxTOpenMessage::StyxTOpenMessage(StyxFID fid, ModeTypeEnum mode) :
	StyxMessage(Topen, NOTAG ){
	mFID = fid;
	mMode = mode;
}

StyxTOpenMessage::~StyxTOpenMessage() {
	// TODO Auto-generated destructor stub
}
StyxFID StyxTOpenMessage::getFID() {
	return mFID;
}
StyxMode StyxTOpenMessage::getMode() {
	return mMode;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTOpenMessage::load(IStyxDataReader *input) {
	mFID = input->readUInt32();
    mMode = (StyxMode)input->readUInt8();
}
size_t StyxTOpenMessage::writeToBuffer(IStyxDataWriter *outputBuffer) {
	StyxMessage::writeToBuffer(outputBuffer);
	outputBuffer->writeUInt32(mFID);
	outputBuffer->writeUInt8(mMode);
    return getBinarySize();
}
size_t StyxTOpenMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + sizeof(StyxFID)+1;
}
