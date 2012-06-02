/*
 * StyxTAttachMessage.cpp
 *
 *  Created on: Jun 1, 2012
 *      Author: mrco
 */

#include "StyxTAttachMessage.h"

StyxTAttachMessage::StyxTAttachMessage(uint32_t fid, uint32_t afid,
		std::string *username, std::string *mountpoint):
		StyxMessage( Tattach, StyxMessage::NOTAG ) {
	mFID = fid;
	mAuthFID = afid;
	mUserName = username;
	mMountPoint = mountpoint;
}

StyxTAttachMessage::~StyxTAttachMessage() {
	// TODO Auto-generated destructor stub
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTAttachMessage::load(IStyxDataReader *input) {
	mFID = input->readUInt32();
    mAuthFID = input->readUInt32();
    mUserName = input->readUTFString();
    mMountPoint = input->readUTFString();
}
size_t StyxTAttachMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
    output->writeUInt32(mFID);
    output->writeUInt32(mAuthFID);
    output->writeUTFString(mUserName);
    output->writeUTFString(mMountPoint);
}
size_t StyxTAttachMessage::getBinarySize() {
	size_t res= StyxMessage::getBinarySize() + 8
		+ 2 + mUserName->length() +
		+ 2 + mMountPoint->length();
	return res;
}
