/*
 * StyxRVersionMessage.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRVERSIONMESSAGE_H_
#define STYXRVERSIONMESSAGE_H_
#include "messages/base/StyxMessage.h"
#include "types.h"

class StyxRVersionMessage : public StyxMessage {
private:
	uint32_t mIOUnit;
	StyxString mProtocol;
public:
	StyxRVersionMessage(size_t iounit, StyxString protocol);
	virtual ~StyxRVersionMessage();
	size_t getMaxPacketSize();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRVERSIONMESSAGE_H_ */
