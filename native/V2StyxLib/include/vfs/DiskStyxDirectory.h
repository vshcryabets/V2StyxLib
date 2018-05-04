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
	virtual IVirtualStyxFile* walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID*> *qids);
	virtual StyxQID createFile(StyxString name, long permissions, int mode) throw(StyxErrorMessageException);
	virtual void deleteFile(ClientDetails* clientDetails) throw(StyxErrorMessageException);

};

#endif /* DISKSTYXDIRECTORY_H_ */
