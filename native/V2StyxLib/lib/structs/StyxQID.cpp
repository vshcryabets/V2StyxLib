/*
 * StyxQID.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "structs/StyxQID.h"

StyxQID::EMPTY = new StyxQID(QIDType.QTFILE, 0L, ULong.ZERO);

StyxQID::StyxQID(IStyxDataReader *input) {
	mType = input->readUInt8();
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
