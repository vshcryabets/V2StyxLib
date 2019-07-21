/*
 * StyxTOpenMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#ifndef STYXTOPENMESSAGE_H_
#define STYXTOPENMESSAGE_H_

#include "messages/base/StyxTMessageFID.h"
#include "../types.h"

class StyxTOpenMessage: public StyxTMessageFID {
private:
	StyxMode mMode;
public:
	StyxTOpenMessage(StyxFID fid, ModeTypeEnum mode);
	virtual ~StyxTOpenMessage();
	StyxMode getMode();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter *outputBuffer);
	virtual size_t getBinarySize();
};

#endif /* STYXTOPENMESSAGE_H_ */
