/*
 * StyxTWrite.h
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#ifndef STYXTWRITE_H_
#define STYXTWRITE_H_

#include "StyxMessage.h"

class StyxTWriteMessage: public StyxMessage {
private:
	StyxFID mFID;
	uint64_t mOffset;
	uint8_t *mData;
	uint32_t mDataLength;
	bool mDelete;
public:
	StyxTWriteMessage();
	~StyxTWriteMessage();
	StyxFID getFID();
	uint8_t* getData();
	uint64_t getOffset();
	uint32_t getCount();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTWRITE_H_ */
