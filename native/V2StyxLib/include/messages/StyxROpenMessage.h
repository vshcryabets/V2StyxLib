/*
 * StyxROpenMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#ifndef STYXROPENMESSAGE_H_
#define STYXROPENMESSAGE_H_

#include "messages/base/StyxMessage.h"
#include "messages/base/structs/StyxQID.h"

class StyxROpenMessage: public StyxMessage {
private:
	StyxQID *mQID;
	size_t mIOUnit;
	bool mDelete;
public:
	StyxROpenMessage(StyxTAG tag, StyxQID* qid, size_t iounit);
	virtual ~StyxROpenMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual size_t writeToBuffer(IStyxDataWriter *outputBuffer);
	virtual size_t getBinarySize();
};

#endif /* STYXROPENMESSAGE_H_ */
