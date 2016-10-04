/*
 * StyxStat.h
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXSTAT_H_
#define STYXSTAT_H_
#include "types.h"
#include "io/IStyxDataReader.h"
#include "io/IStyxDataWriter.h"
#include "StyxQID.h"

class StyxStat {
private:
	uint16_t mType; //for kernel use
	uint32_t mDev; //for kernel use
	StyxQID *mQID;
	uint32_t mMode; // permissions and flags
	Date mAccessTime; // last access time
	Date mModificationTime; // last modification time
	uint64_t mLength; //length of file in bytes
	StyxString mName; // file name; must be / if the file is the root directory of the server
	StyxString mUserName; //owner name
	StyxString mGroupName; //group name
	StyxString mModificationUser; //name of the user who last modified the file
	bool mNeedDelete;
public:
	StyxStat(IStyxDataReader *input);
	StyxStat(uint16_t type, uint32_t dev, StyxQID *qid, uint32_t mode, Date accessTime,
            Date modificationTime, uint64_t length, StyxString name, StyxString userName,
            StyxString groupName, StyxString modificationUser);
	virtual ~StyxStat();
	uint16_t getSize();
	size_t writeBinaryTo(IStyxDataWriter *output);
	void setMode(uint32_t mode);
};

#endif /* STYXSTAT_H_ */
