/*
 * StyxRStatMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRSTATMESSAGE_H_
#define STYXRSTATMESSAGE_H_

#include "StyxMessage.h"
#include "../structs/StyxStat.h"

class StyxRStatMessage: public StyxMessage {
private:
	StyxStat *mStat;
public:
	StyxRStatMessage(StyxTAG tag, StyxStat *stat);
	virtual ~StyxRStatMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRSTATMESSAGE_H_ */
