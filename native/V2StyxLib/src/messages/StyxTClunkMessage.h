/*
 * StyxTClunkMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#ifndef STYXTCLUNKMESSAGE_H_
#define STYXTCLUNKMESSAGE_H_

#include "StyxMessage.h"

class StyxTClunkMessage: public StyxMessage {
private:
	StyxFID mFID;
public:
	StyxTClunkMessage(StyxFID fid);
	virtual ~StyxTClunkMessage();
	StyxFID getFID();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTCLUNKMESSAGE_H_ */
