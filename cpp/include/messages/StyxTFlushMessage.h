/*
 * StyxTFlushMessage.h
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#ifndef STYXTFLUSHMESSAGE_H_
#define STYXTFLUSHMESSAGE_H_

#include "StyxMessage.h"

class StyxTFlushMessage: public StyxMessage {
private:
	StyxTAG mOldTag;
public:
	StyxTFlushMessage(StyxTAG oldtag);
	~StyxTFlushMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTFLUSHMESSAGE_H_ */
