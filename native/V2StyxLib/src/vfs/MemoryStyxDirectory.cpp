/*
 * MemoryStyxDirectory.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vshcryabets@gmail.com
 */

#include "MemoryStyxDirectory.h"
#include "../types.h"
#include "../structs/StyxQID.h"

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
	return false;
}
/**
 * Close file
 * @param mode
 */
void MemoryStyxDirectory::close(ClientState *client) {

}
/**
 * Read from file
 * @param offset offset from begining of the file
 * @param count number of bytes to read
 * @return number of bytes that was readed into the buffer
 */
size_t MemoryStyxDirectory::read(ClientState *client, uint8_t* buffer, uint64_t offset, size_t count) {
	return 0;
}
IVirtualStyxFile* MemoryStyxDirectory::walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID*> *qids) {
	if ( pathElements->size() < 1 ) {
		return this;
	} else {
		StyxString* filename = *(pathElements->begin());
		for ( vector<IVirtualStyxFile*>::iterator iterator = mFiles->begin(); iterator < mFiles->end(); iterator++ ) {
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
 * @throws StyxErrorMessageException
 */
int MemoryStyxDirectory::write(ClientState *client, uint8_t* data, uint64_t offset) {
	return 0;
}

