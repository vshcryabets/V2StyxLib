/*
 * StyxStat.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#include "structs/StyxStat.h"

StyxStat::StyxStat(IStyxDataReader *input) {
	//uint16_t size =
	input->readUInt16(); // skip size bytes
	mType = input->readUInt16();
	mDev = input->readUInt32();
	mQID = new StyxQID(input);
	mMode = input->readUInt32();
	mAccessTime = input->readUInt32();
	mModificationTime = input->readUInt32();
	mLength = input->readUInt64();
	mName = input->readUTFString();
	mUserName = input->readUTFString();
	mGroupName = input->readUTFString();
	mModificationUser = input->readUTFString();
	mNeedDelete = true;
}
StyxStat::StyxStat(uint16_t type, uint32_t dev, StyxQID *qid, uint32_t mode, Date accessTime,
		Date modificationTime, uint64_t length, StyxString name, StyxString userName,
		StyxString groupName, StyxString modificationUser) {
	mType = type;
	mDev = dev;
	mQID = qid;
	mMode = mode;
	mAccessTime = accessTime;
	mModificationTime = modificationTime;
	mLength = length;
	mName = name;
	mUserName = userName;
	mGroupName = groupName;
	mModificationUser = modificationUser;
	mNeedDelete = false;
}
StyxStat::~StyxStat() {
	if ( mNeedDelete ) {
		delete mQID;
		mQID = NULL;
	}
}
uint16_t StyxStat::getSize() {
	return 28 + StyxQID::CONTENT_SIZE
			+ 2 + mName.length() +
			+ 2 + mUserName.length() +
			+ 2 + mGroupName.length() +
			+ 2 + mModificationUser.length();
}
size_t StyxStat::writeBinaryTo(IStyxDataWriter *output) {
	output->writeUInt16(getSize() - 2); // TODO -2??? what does it mean?
	output->writeUInt16(mType);
	output->writeUInt32(mDev);
	mQID->writeBinaryTo(output);
	output->writeUInt32(mMode);
	output->writeUInt32(mAccessTime);
	output->writeUInt32(mModificationTime);
	output->writeUInt64(mLength);
	output->writeUTFString(&mName);
	output->writeUTFString(&mUserName);
	output->writeUTFString(&mGroupName);
	output->writeUTFString(&mModificationUser);
	return getSize();
}
void StyxStat::setMode(uint32_t mode) {
	mMode = mode;
}
