/*
 * StyxQID.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/base/structs/StyxQID.h"

const StyxQID* StyxQID::EMPTY = new StyxQID(QTFILE, 0L, 0L);

StyxQID::StyxQID(IStyxDataReader *input) {
	mType = (QIDTypeEnum)input->readUInt8();
	mVersion = input->readUInt32();
	mPath = input->readUInt64();
}
StyxQID::StyxQID(QIDTypeEnum type, uint32_t version, uint64_t path) {
	mType = type;
	mVersion = version;
	mPath = path;
}
StyxQID::~StyxQID() {
	// TODO Auto-generated destructor stub
}

void StyxQID::writeBinaryTo(IStyxDataWriter *output) {
	output->writeUInt8(mType);
	output->writeUInt32(mVersion);
	output->writeUInt64(mPath);
}

void StyxQID::setType(QIDTypeEnum type) {
	mType = type;
}

StyxString StyxQID::toString() {
	char buffer[4096];
	snprintf(buffer, sizeof(buffer), "(Type: %d; Version: %d; Path: %llu)",
			getType(), getVersion(), getPath());
	return StyxString(buffer);
}

QIDTypeEnum StyxQID::getType() const {
	return mType;
}

uint32_t StyxQID::getVersion() const {
	return mVersion;
}

uint64_t StyxQID::getPath() const {
	return mPath;
}
