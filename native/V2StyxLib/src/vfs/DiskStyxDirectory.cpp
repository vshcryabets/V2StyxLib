/*
 * DiskStyxDirectory.cpp
 *
 */
#include "vfs/DiskStyxDirectory.h"

DiskStyxDirectory::DiskStyxDirectory(std::string name):
	DiskStyxFile(name) {
	mQID.setType(QTDIR);
	mStat->setMode(getMode());
}

DiskStyxDirectory::~DiskStyxDirectory() {
}

IVirtualStyxFile* DiskStyxDirectory::walk(
		std::vector<StyxString*> *pathElements,
		std::vector<StyxQID*> *qids) {
	return NULL;
}

StyxQID DiskStyxDirectory::createFile(StyxString name, long permissions, int mode) throw(StyxErrorMessageException) {
	throw StyxErrorMessageException("Not implemented");
}

void DiskStyxDirectory::deleteFile(ClientDetails* clientDetails) throw(StyxErrorMessageException) {
	throw StyxErrorMessageException("Not implemented");
}
