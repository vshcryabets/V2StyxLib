/*
 * StyxTWrite.h
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#ifndef STYXTWRITE_H_
#define STYXTWRITE_H_

#include "messages/base/StyxTMessageFID.h"

class StyxTWriteMessage: public StyxTMessageFID {
private:
	uint64_t mOffset;
	uint8_t *mData;
	uint32_t mDataLength;
	bool mDelete;
public:
	StyxTWriteMessage(StyxFID fid, uint64_t offset, uint8_t *data, uint32_t length);
	~StyxTWriteMessage();
	uint8_t* getData();
	uint64_t getOffset();
	uint32_t getCount();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTWRITE_H_ */
