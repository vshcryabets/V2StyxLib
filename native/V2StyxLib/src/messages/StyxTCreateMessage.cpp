/*
 * StyxTClunkMessage.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "messages/StyxTCreateMessage.h"

StyxTCreateMessage::StyxTCreateMessage(StyxFID fid, StyxString name, uint32_t permissions, uint16_t mode) :
	StyxTMessageFID(Tcreate, Rcreate, fid), mName(name), mPermissions(permissions), mMode(mode) {
}

StyxTCreateMessage::~StyxTCreateMessage() {
}

StyxString StyxTCreateMessage::getName() {
	return mName;
}

void StyxTCreateMessage::setName(StyxString name) {
	mName = name;
}

uint32_t StyxTCreateMessage::getPermissions()
{
	return mPermissions;
}

void StyxTCreateMessage::setPermissions(uint32_t permissions)
{
	mPermissions = permissions;
}

uint16_t StyxTCreateMessage::getMode() {
	return mMode;
}

void StyxTCreateMessage::setMode(uint16_t mode) {
	mMode = mode;
}

// =======================================================
// Virtual methods
// =======================================================
void StyxTCreateMessage::load(IStyxDataReader *input) {
    mName = input->readUTFString();
    mPermissions = input->readUInt32();
    mMode = input->readUInt8();
}

size_t StyxTCreateMessage::getBinarySize() {
	return StyxTMessageFID::getBinarySize() + 5
			+ StyxMessage::getUTFSize(this->getName());
}

void StyxTCreateMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxTMessageFID::writeToBuffer(output);
	output->writeUTFString(mName);
	output->writeUInt32(getPermissions());
	output->writeUInt8(getMode());
}

