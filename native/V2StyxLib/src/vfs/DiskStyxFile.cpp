/*
 * DiskStyxFile.cpp
 *
 */

#include "vfs/DiskStyxFile.h"

DiskStyxFile::DiskStyxFile(std::string name):
	MemoryStyxFile(name) {
	mQID->setType(QTDIR);
	mStat->setMode(getMode());
}

DiskStyxFile::~DiskStyxFile() {
}

