/*
 * StyxTStatMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxTStatMessage.h"

StyxTStatMessage::StyxTStatMessage(StyxFID fid) :
	StyxMessage(Tstat, NOTAG ){
	mFID = fid;
}

StyxTStatMessage::~StyxTStatMessage() {
	// TODO Auto-generated destructor stub
}
StyxFID StyxTStatMessage::getFID() {
	return mFID;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTStatMessage::load(IStyxDataReader *buffer) {
	mFID = buffer->readUInt32();
}
size_t StyxTStatMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
    output->writeUInt32(getFID());
}
size_t StyxTStatMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + 4;
}
