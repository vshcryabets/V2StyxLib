/*
 * MemoryStyxFile.cpp
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#include "vfs/MemoryStyxFile.h"
#include "messages/base/structs/StyxQID.h"

MemoryStyxFile::MemoryStyxFile(std::string name)
	: mQID(QTFILE, 0, (uint64_t)this) {
	mName = name;
	mOwner = "nobody";
	mStat = new StyxStat(0,
			1,
			mQID,
			getMode(),
			getAccessTime(),
			getModificationTime(),
			getLength(),
			name,
			mOwner,
			mOwner,
			mOwner);
}

MemoryStyxFile::~MemoryStyxFile() {
	delete mStat;
}

/**
 * @return unic ID of the file
 */
StyxQID MemoryStyxFile::getQID() {
	return mQID;
}

StyxStat* MemoryStyxFile::getStat() {
	return mStat;
}
/**
 * @return file name
 */
StyxString MemoryStyxFile::getName() {
	return mName;
}
Date MemoryStyxFile::getAccessTime() {
	return 0;
}
Date MemoryStyxFile::getModificationTime() {
	return 0;
}
uint64_t MemoryStyxFile::getLength() {
	return 0;
}

StyxString MemoryStyxFile::getOwnerName() {
	return mOwner;
}
StyxString MemoryStyxFile::getGroupName() {
	return mOwner;
}
StyxString MemoryStyxFile::getModificationUser() {
	return mOwner;
}
/**
 * @return file access mode
 */
int MemoryStyxFile::getMode() {
	return 0x01FF;
}

bool MemoryStyxFile::open(ClientDetails *client, int mode) {
	bool result = (
			(mode&0x0F) == OREAD ||
			(mode&0x0F) == OWRITE ||
			(mode&0x0F) == ORDWR
	);
	return result;
}
IVirtualStyxFile* MemoryStyxFile::walk(std::vector<StyxString> *pathElements, std::vector<StyxQID> *qids) {
	return this;
}
size_t MemoryStyxFile::write(ClientDetails *client, uint8_t* data, uint64_t offset, size_t count) {
	return 0;
}
size_t MemoryStyxFile::read(ClientDetails *client, uint8_t* buffer, uint64_t offset, size_t count) {
	return 0;
}

void MemoryStyxFile::close(ClientDetails *client) {
}

void MemoryStyxFile::onConnectionClosed(ClientDetails *state) {
	// ok, nothing to do
}

void MemoryStyxFile::onConnectionOpened(ClientDetails* client) {
	// ok, nothing to do
}