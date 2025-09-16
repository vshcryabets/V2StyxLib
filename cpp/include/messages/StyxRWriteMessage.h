/*
 * StyxRWriteMessage.h
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#ifndef STYXRWRITEMESSAGE_H_
#define STYXRWRITEMESSAGE_H_

#include "StyxMessage.h"

class StyxRWriteMessage: public StyxMessage {
private:
	uint32_t mCount;
public:
	StyxRWriteMessage(StyxTAG tag, size_t count);
	~StyxRWriteMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRWRITEMESSAGE_H_ */
