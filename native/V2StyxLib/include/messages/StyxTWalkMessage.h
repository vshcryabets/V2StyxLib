/*
 * StyxTWalkMessage.h
 *
 *  Created on: Jun 2, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXTWALKMESSAGE_H_
#define STYXTWALKMESSAGE_H_

#include "messages/base/StyxTMessageFID.h"
#include <vector>

class StyxTWalkMessage: public StyxTMessageFID {
private:
	StyxFID mNewFID;
	StyxString mPath;
	std::vector<StyxString*> *mPathElements;
public:
	StyxTWalkMessage(StyxFID fid, StyxFID new_fid);
	virtual ~StyxTWalkMessage();
	uint32_t getNewFID();
	std::vector<StyxString*>* getPathElements();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTWALKMESSAGE_H_ */
