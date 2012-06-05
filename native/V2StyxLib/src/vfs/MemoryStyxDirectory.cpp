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
	size_t remaining = dataSize - offset;
	if ( count > remaining ) {
		count = remaining;
	}
	memcpy(buffer, preparedData->getBuffer()+offset, count);
	return count;
}
IVirtualStyxFile* MemoryStyxDirectory::walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID*> *qids) {
	if ( pathElements->size() > 0 ) {
		// TODO something are wrong in this logic, why we don't put to result QIDs this->mQID?
		StyxString* filename = *(pathElements->begin());
		for ( FileList::iterator iterator = mFiles.begin(); iterator < mFiles.end(); iterator++ ) {
			StyxString itemFileName = (*iterator)->getName();
			if ( itemFileName.compare(*filename) == 0 ) {
				pathElements->erase(pathElements->begin());
				return (*iterator)->walk(pathElements, qids);
			}
		}
	}
	return MemoryStyxFile::walk(pathElements, qids);
}
void MemoryStyxDirectory::onConnectionClosed(ClientState *state) {
	for ( FileList::iterator it = mFiles.begin();
			it != mFiles.end(); it++ ) {
		(*it)->onConnectionClosed(state);
	}
	close(state);
}
void MemoryStyxDirectory::addFile(IVirtualStyxFile *file) {
    mFiles.push_back(file);
}
