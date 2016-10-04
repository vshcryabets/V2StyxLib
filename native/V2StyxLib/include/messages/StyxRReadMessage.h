/*
 * StyxRReadMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRREADMESSAGE_H_
#define STYXRREADMESSAGE_H_

#include "messages/base/StyxMessage.h"

class StyxRReadMessage: public StyxMessage {
private:
	uint8_t *mData;
	size_t mDataLength;
	bool mDelete;
public:
	StyxRReadMessage(StyxTAG tag, uint8_t *data, size_t length);
	virtual ~StyxRReadMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRREADMESSAGE_H_ */
