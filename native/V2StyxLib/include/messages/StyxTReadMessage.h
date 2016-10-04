/*
 * StyxTReadMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXTREADMESSAGE_H_
#define STYXTREADMESSAGE_H_

#include "messages/base/StyxTMessageFID.h"

class StyxTReadMessage: public StyxTMessageFID {
private:
	uint64_t mOffset;
	uint32_t mCount;
public:
	StyxTReadMessage(StyxFID fid, uint64_t offset, uint32_t count);
	virtual ~StyxTReadMessage();
	uint64_t getOffset();
	uint32_t getCount();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTREADMESSAGE_H_ */
