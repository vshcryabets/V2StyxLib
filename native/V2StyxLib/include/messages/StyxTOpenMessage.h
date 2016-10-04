/*
 * StyxTOpenMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#ifndef STYXTOPENMESSAGE_H_
#define STYXTOPENMESSAGE_H_

#include "messages/base/StyxMessage.h"
#include "../types.h"

class StyxTOpenMessage: public StyxMessage {
private:
	StyxFID mFID;
	StyxMode mMode;
public:
	StyxTOpenMessage(StyxFID fid, ModeTypeEnum mode);
	virtual ~StyxTOpenMessage();
	StyxFID getFID();
	StyxMode getMode();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual size_t writeToBuffer(IStyxDataWriter *outputBuffer);
	virtual size_t getBinarySize();
};

#endif /* STYXTOPENMESSAGE_H_ */
