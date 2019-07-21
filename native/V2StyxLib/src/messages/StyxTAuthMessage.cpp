/*
 * StyxTAuthMessage.cpp
 *
 *  Created on: Apr 7, 2018
 *      Author: mrco
 */

#include "messages/StyxTAuthMessage.h"
#include <sstream>

StyxTAuthMessage::StyxTAuthMessage(StyxFID fid) :
	StyxTMessageFID(Tauth, Rauth, fid),
    mUserName(""),
	mMountPoint("") {
}

StyxTAuthMessage::~StyxTAuthMessage() {

}

StyxString StyxTAuthMessage::getMountPoint() {
	return mMountPoint;
}

StyxString StyxTAuthMessage::getUserName() {
	return mUserName;
}

void StyxTAuthMessage::setUserName(StyxString userName) {
	mUserName = userName;
}

void StyxTAuthMessage::setMountPoint(StyxString mountPoint) {
	mMountPoint = mountPoint;
}

void StyxTAuthMessage::load(IStyxDataReader *buffer) {
	StyxTMessageFID::load(buffer);
	setUserName(buffer->readUTFString());
	setMountPoint(buffer->readUTFString());
}

void StyxTAuthMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxTMessageFID::writeToBuffer(output);
	output->writeUTFString(getUserName());
	output->writeUTFString(getMountPoint());
}

size_t StyxTAuthMessage::getBinarySize() {
	return StyxTMessageFID::getBinarySize()
		+ StyxMessage::getUTFSize(getUserName())
		+ StyxMessage::getUTFSize(getMountPoint());
}

StyxString StyxTAuthMessage::toString() {
	std::stringstream stream;
	stream << StyxTMessageFID::toString() << "\nUserName: "
			<< getUserName() << "\nMountPoint: " << getMountPoint();
	return stream.str();
}
