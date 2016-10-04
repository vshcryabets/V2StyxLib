/*
 * StyxTAttachMessage.cpp
 *
 *  Created on: Jun 1, 2012
 *      Author: mrco
 */

#include "messages/StyxTAttachMessage.h"

StyxTAttachMessage::StyxTAttachMessage(uint32_t fid, uint32_t afid,
		std::string username, std::string mountpoint):
		StyxTMessageFID( Tattach, Rattach, fid ) {
	mAuthFID = afid;
	mUserName = username;
	mMountPoint = mountpoint;
}

StyxTAttachMessage::~StyxTAttachMessage() {
}
std::string StyxTAttachMessage::getMountPoint() {
	return mMountPoint;
}
std::string StyxTAttachMessage::getUserName() {
	return mUserName;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxTAttachMessage::load(IStyxDataReader *input) {
	StyxTMessageFID::load(input);
    mAuthFID = input->readUInt32();
    mUserName = input->readUTFString();
    mMountPoint = input->readUTFString();
}
void StyxTAttachMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxTMessageFID::writeToBuffer(output);
    output->writeUInt32(mAuthFID);
    output->writeUTFString(&mUserName);
    output->writeUTFString(&mMountPoint);
}
size_t StyxTAttachMessage::getBinarySize() {
	size_t res= StyxTMessageFID::getBinarySize()
		+ 2 + mUserName.length() +
		+ 2 + mMountPoint.length();
	return res;
}
