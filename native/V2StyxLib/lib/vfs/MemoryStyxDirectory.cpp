/*
 * MemoryStyxDirectory.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vshcryabets@gmail.com
 */

#include "vfs/MemoryStyxDirectory.h"
#include "types.h"
#include "messages/base/structs/StyxQID.h"
#include <vector>
#include "string.h"
#include "io/StyxDataWriter.h"

MemoryStyxDirectory::MemoryStyxDirectory(std::string name):
	MemoryStyxFile(name) {
	mQID->setType(QTDIR);
	mStat->setMode(getMode());
}

MemoryStyxDirectory::~MemoryStyxDirectory() {
}
/**
 * @return file access mode
 */
int MemoryStyxDirectory::getMode() {
	return Directory | 0x01FF;
}
/**
 * Open file
 * @param mode
 * @throws IOException
 */
bool MemoryStyxDirectory::open(ClientDetails *client, int mode){
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
		uint8_t* buffer = new uint8_t(size);
		StyxDataWriter writer(buffer);
		for ( std::vector<StyxStat*>::iterator it = stats.begin();
				it != stats.end(); it++ ) {
			(*it)->writeBinaryTo(&writer);
		}
		mBuffersMap.insert(std::pair<ClientDetails*, uint8_t*>(client, buffer));
	}
	return result;
}
/**
 * Close file
 * @param mode
 */
void MemoryStyxDirectory::close(ClientDetails *client) {
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
size_t MemoryStyxDirectory::read(ClientDetails *client, uint8_t* buffer, uint64_t offset, size_t count) {
	ClientsMap::iterator it = mBuffersMap.find(client);
	if ( it == mBuffersMap.end() ) {
		return -1; // TODO there we should send Rerror with message "This file isn't open"
	}
	uint8_t* preparedData = it->second;
	size_t dataSize = preparedData->getCapacity();
	if ( offset > dataSize ) return 0;
	size_t remaining = dataSize - offset;
	if ( count > remaining ) {
		count = remaining;
	}
	memcpy(buffer, preparedData->getBuffer()+offset, count);
	return count;
}
IVirtualStyxFile* MemoryStyxDirectory::walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID*> *qids) {
	if ( pathElements->size() > 0 ) {
		StyxString* filename = *(pathElements->begin());
		for ( FileList::iterator iterator = mFiles.begin(); iterator < mFiles.end(); iterator++ ) {
			StyxString itemFileName = (*iterator)->getName();
			if ( itemFileName.compare(*filename) == 0 ) {
				pathElements->erase(pathElements->begin());
				qids->push_back((*iterator)->getQID());
				return (*iterator)->walk(pathElements, qids);
			}
		}
		return NULL;
	}
	return MemoryStyxFile::walk(pathElements, qids);
}
void MemoryStyxDirectory::onConnectionClosed(ClientDetails *state) {
	for ( FileList::iterator it = mFiles.begin();
			it != mFiles.end(); it++ ) {
		(*it)->onConnectionClosed(state);
	}
	close(state);
}
void MemoryStyxDirectory::addFile(IVirtualStyxFile *file) {
    mFiles.push_back(file);
}
