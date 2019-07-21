/*
 * DiskStyxFile.h
 */

#ifndef DISKSTYXFILE_H_
#define DISKSTYXFILE_H_

#include "vfs/MemoryStyxFile.h"

class DiskStyxFile : public MemoryStyxFile
{
private:
public:
	DiskStyxFile(std::string name);
	virtual ~DiskStyxFile();
};

#endif /* DISKSTYXFILE_H_ */
