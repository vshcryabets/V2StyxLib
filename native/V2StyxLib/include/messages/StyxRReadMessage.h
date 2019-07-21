/*
 * StyxRReadMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRREADMESSAGE_H_
#define STYXRREADMESSAGE_H_

#include "messages/base/StyxMessage.h"
#include "types.h"

class StyxRReadMessage: public StyxMessage {
private:
	StyxBuffer mData;
	size_t mDataLength;
public:
	StyxRReadMessage(StyxTAG tag, StyxBuffer data, size_t length);
	virtual ~StyxRReadMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRREADMESSAGE_H_ */
