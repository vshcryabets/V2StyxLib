/*
 * MemoryStyxDirectory.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vshcryabets@gmail.com
 */

#include "MemoryStyxDirectory.h"
#include "../types.h"
#include "../structs/StyxQID.h"
#include <vector>
#include "string.h"

MemoryStyxDirectory::MemoryStyxDirectory(std::string name):mName(name) {
	mQID = new StyxQID(QTDIR, 0, (uint64_t)this);
	mOwner = new StyxString("nobody");
	mStat = new StyxStat(0,
			1,
			mQID,
			getMode(),
			getAccessTime(),
			getModificationTime(),
			getLength(),
			name,
			*mOwner,
			*mOwner,
			*mOwner);
}

MemoryStyxDirectory::~MemoryStyxDirectory() {
	delete mQID;
	delete mStat;
}

IVirtualStyxFile* MemoryStyxDirectory::getFile(string *path) {
	return NULL;
}
IVirtualStyxDirectory* MemoryStyxDirectory::getDirectory(string *path) {
	if ( path->length() == 0 || path->compare("/")) return this;
	return NULL;
}

/**
 * @return unic ID of the file
 */
StyxQID* MemoryStyxDirectory::getQID() {
	return mQID;
}

StyxStat* MemoryStyxDirectory::getStat() {
	return mStat;
}
/**
 * @return file access mode
 */
int MemoryStyxDirectory::getMode() {
	return 0x01FF;
}
/**
 * @return file name
 */
StyxString* MemoryStyxDirectory::getName() {
	return &mName;
}
Date MemoryStyxDirectory::getAccessTime() {
	return 0;
}
Date MemoryStyxDirectory::getModificationTime() {
	return 0;
}
uint64_t MemoryStyxDirectory::getLength() {
	return 0;
}

StyxString* MemoryStyxDirectory::getOwnerName() {
	return mOwner;
}
StyxString* MemoryStyxDirectory::getGroupName() {
	return mOwner;
}
StyxString* MemoryStyxDirectory::getModificationUser() {
	return mOwner;
}
/**
 * Open file
 * @param mode
 * @throws IOException
 */
bool MemoryStyxDirectory::open(ClientState *client, int mode){
	bool result = ((mode&0x0F) == OREAD);
	if ( result ) {
		// prepare binary structure of the directory
		size_t size = 0;
		std::vector<StyxStat*> stats;
		for ( FileList::iterator it = mFiles.begin();
				it != mFiles.end(); it++ ) {
			StyxStat *stat = (*it)->getStat();
			size += stat->getSize();
			stats.push_back(stat);
		}
		// allocate buffer
		StyxByteBufferWritable *buffer = new StyxByteBufferWritable(size);
		for ( std::vector<StyxStat*>::iterator it = stats.begin();
				it != stats.end(); it++ ) {
			(*it)->writeBinaryTo(buffer);
		}
		mBuffersMap.insert(std::pair<ClientState*, StyxByteBufferWritable*>(client, buffer));
	}
	return result;
}
/**
 * Close file
 * @param mode
 */
void MemoryStyxDirectory::close(ClientState *client) {
	// remove buffer
	ClientsMap::iterator item = mBuffersMap.find(client);
	if ( item != mBuffersMap.end() ) {
		delete item->second;
		mBuffersMap.erase(item);
	}
}
/**
 * Read from file
 * @param offset offset from begining of the file
 * @param count number of bytes to read
 * @return number of bytes that was readed into the buffer
 */
size_t MemoryStyxDirectory::read(ClientState *client, uint8_t* buffer, uint64_t offset, size_t count) {
	ClientsMap::iterator it = mBuffersMap.find(client);
	if ( it == mBuffersMap.end() ) {
		return -1; // TODO there we should send Rerror with message "This file isn't open"
	}
	StyxByteBufferWritable *preparedData = it->second;
	size_t dataSize = preparedData->getCapacity();
	if ( offset > dataSize ) return 0;
	int remaining = dataSize - offset;
	if ( count > remaining ) {
		count = remaining;
	}
	memcpy(buffer, preparedData->getBuffer()+offset, count);
	return count;
}
IVirtualStyxFile* MemoryStyxDirectory::walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID*> *qids) {
	if ( pathElements->size() < 1 ) {
		return this;
	} else {
		StyxString* filename = *(pathElements->begin());
		for ( FileList::iterator iterator = mFiles.begin(); iterator < mFiles.end(); iterator++ ) {
			if ( (*iterator)->getName()->compare(*filename)) {
				pathElements->erase(pathElements->begin());
				qids->push_back((*iterator)->getQID());
				return (*iterator)->walk(pathElements, qids);
			}
		}
	}
	return NULL;
}
/**
 * Write data to file
 * @param client
 * @param data
 * @param offset
 * @return
 */
int MemoryStyxDirectory::write(ClientState *client, uint8_t* data, uint64_t offset) {
	return 0;
}
void MemoryStyxDirectory::onConnectionClosed(ClientState *state) {
	for ( FileList::iterator it = mFiles.begin();
			it != mFiles.end(); it++ ) {
		(*it)->onConnectionClosed(state);
	}
	close(state);
}
