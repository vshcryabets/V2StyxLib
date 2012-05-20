/*
 * MemoryStyxDirectory.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vshcryabets@gmail.com
 */

#include "MemoryStyxDirectory.h"

MemoryStyxDirectory::MemoryStyxDirectory(std::string name):mName(name) {
}

MemoryStyxDirectory::~MemoryStyxDirectory() {
	// TODO Auto-generated destructor stub
}

IVirtualStyxFile* MemoryStyxDirectory::getFile(string path) {
	return NULL;
}
IVirtualStyxDirectory* MemoryStyxDirectory::getDirectory(string path) {
	return NULL;
}

/**
 * @return unic ID of the file
 */
StyxQID* MemoryStyxDirectory::getQID() {
	return NULL;
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
bool MemoryStyxDirectory::open(ClientState client, int mode){
	return false;
}
/**
 * Close file
 * @param mode
 */
void MemoryStyxDirectory::close(ClientState client) {

}
/**
 * Read from file
 * @param offset offset from begining of the file
 * @param count number of bytes to read
 * @return number of bytes that was readed into the buffer
 */
long MemoryStyxDirectory::read(ClientState client, int8_t* buffer, int128_t offset, long count)
{
	return 0;
}
//	IVirtualStyxFile walk(List<String> pathElements, List<StyxQID> qids) = 0;
/**
 * Write data to file
 * @param client
 * @param data
 * @param offset
 * @return
 * @throws StyxErrorMessageException
 */
int MemoryStyxDirectory::write(ClientState client, int8_t* data, int128_t offset) {
	return 0;
}

