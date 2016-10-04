/*
 * StyxTWStatMessage.h
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#ifndef STYXTWSTATMESSAGE_H_
#define STYXTWSTATMESSAGE_H_

#include "messages/base/StyxMessage.h"
#include "messages/base/structs/StyxStat.h"

class StyxTWStatMessage: public StyxMessage {
private:
	StyxFID mFID;
	StyxStat *mStat;
	bool mDelete;
public:
	StyxTWStatMessage(StyxFID fid, StyxStat *stat);
	~StyxTWStatMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTWSTATMESSAGE_H_ */
