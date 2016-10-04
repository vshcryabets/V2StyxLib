/*
 * StyxTClunkMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "../../include/messages/StyxTCreateMessage.h"

StyxTCreateMessage::StyxTCreateMessage(StyxFID fid) :
	StyxMessage(Tclunk, NOTAG){
	mFID = fid;
}

StyxTCreateMessage::~StyxTCreateMessage() {
}
StyxFID StyxTCreateMessage::getFID() {
	return mFID;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTCreateMessage::load(IStyxDataReader *input) {
	mFID = input->readUInt32();
}
size_t StyxTCreateMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt32(mFID);
	return getBinarySize();
}
size_t StyxTCreateMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + sizeof(mFID);
}
