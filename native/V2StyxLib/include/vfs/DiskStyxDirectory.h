/*
 * DiskStyxDirectory.h
 */

#ifndef DISKSTYXDIRECTORY_H_
#define DISKSTYXDIRECTORY_H_

#include "vfs/DiskStyxFile.h"

class DiskStyxDirectory : public DiskStyxFile
{
private:
public:
	DiskStyxDirectory(std::string name);
	virtual ~DiskStyxDirectory();
};

#endif /* DISKSTYXDIRECTORY_H_ */
