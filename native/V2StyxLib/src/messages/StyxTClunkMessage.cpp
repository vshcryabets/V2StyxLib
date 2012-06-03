/*
 * StyxTClunkMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "StyxTClunkMessage.h"

StyxTClunkMessage::StyxTClunkMessage(StyxFID fid) :
	StyxMessage(Tclunk, NOTAG){
	mFID = fid;
}

StyxTClunkMessage::~StyxTClunkMessage() {
}
StyxFID StyxTClunkMessage::getFID() {
	return mFID;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTClunkMessage::load(IStyxDataReader *input) {
	mFID = input->readUInt32();
}
size_t StyxTClunkMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt32(mFID);
	return getBinarySize();
}
size_t StyxTClunkMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + sizeof(mFID);
}
