/*
 * StyxRStatMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRSTATMESSAGE_H_
#define STYXRSTATMESSAGE_H_

#include "messages/base/StyxMessage.h"
#include "messages/base//structs/StyxStat.h"

class StyxRStatMessage: public StyxMessage {
private:
	StyxStat *mStat;
public:
	StyxRStatMessage(StyxTAG tag, StyxStat *stat);
	StyxRStatMessage(StyxTAG tag);
	virtual ~StyxRStatMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRSTATMESSAGE_H_ */
