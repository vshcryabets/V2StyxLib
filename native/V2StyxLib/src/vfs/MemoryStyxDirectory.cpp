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
}

MemoryStyxDirectory::~MemoryStyxDirectory() {
	delete mQID;
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
	return NULL;
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
string MemoryStyxDirectory::getName() {
	return mName;
}
Date MemoryStyxDirectory::getAccessTime() {
	return 0;
}
Date MemoryStyxDirectory::getModificationTime() {
	return 0;
}
int128_t MemoryStyxDirectory::getLength() {
	return 0;
}

string MemoryStyxDirectory::getOwnerName() {
	return "nobody";
}
string MemoryStyxDirectory::getGroupName() {
	return "nobody";
}
string MemoryStyxDirectory::getModificationUser() {
	return "nobody";
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
long MemoryStyxDirectory::read(ClientState *client, int8_t* buffer, int128_t offset, long count)
{
	return 0;
}
IVirtualStyxFile* MemoryStyxDirectory::walk(std::vector<std::string> *pathElements, std::vector<StyxQID> *qids) {
	if ( pathElements->size() < 1 ) {
		return this;
	} else {
		std::string filename = *(pathElements->begin());
		for (IVirtualStyxFile file : mFiles) {
			if ( file.getName().equals(filename)) {
				pathElements.remove(0);
				qids.add(file.getQID());
				if ( file instanceof IVirtualStyxDirectory ) {
					return ((IVirtualStyxDirectory)file).walk(pathElements, qids);
				} else {
					return file;
				}
			}
		}
	}
	return null;
}
/**
 * Write data to file
 * @param client
 * @param data
 * @param offset
 * @return
 * @throws StyxErrorMessageException
 */
 int MemoryStyxDirectory::write(ClientState *client, int8_t* data, int128_t offset) {
	 return 0;
 }

