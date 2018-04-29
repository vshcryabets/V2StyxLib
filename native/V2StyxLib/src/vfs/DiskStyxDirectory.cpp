/*
 * DiskStyxDirectory.cpp
 *
 */
#include "vfs/DiskStyxDirectory.h"

DiskStyxDirectory::DiskStyxDirectory(std::string name):
	DiskStyxFile(name) {
	mQID->setType(QTDIR);
	mStat->setMode(getMode());
}

DiskStyxDirectory::~DiskStyxDirectory() {
}

IVirtualStyxFile* DiskStyxDirectory::walk(
		std::vector<StyxString*> *pathElements,
		std::vector<StyxQID*> *qids) {
	return NULL;
}
