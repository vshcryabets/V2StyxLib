/*
 * StyxROpenMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#ifndef STYXROPENMESSAGE_H_
#define STYXROPENMESSAGE_H_

#include "messages/base/StyxRSingleQIDMessage.h"
#include "messages/base/structs/StyxQID.h"

class StyxROpenMessage: public StyxRSingleQIDMessage {
private:
	size_t mIOUnit;
public:
	StyxROpenMessage(StyxTAG tag, StyxQID qid, size_t iounit, bool create);
	virtual ~StyxROpenMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter *outputBuffer);
	virtual size_t getBinarySize();
};

#endif /* STYXROPENMESSAGE_H_ */
