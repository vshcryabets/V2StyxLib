/*
 * StyxTVersionMessage.h
 *
 *  Created on: May 24, 2012
 *      Author: mrco
 */

#ifndef STYXTVERSIONMESSAGE_H_
#define STYXTVERSIONMESSAGE_H_
#include <string>
#include "../types.h"
#include "messages/base/StyxMessage.h"

class StyxTVersionMessage : public StyxMessage {
private:
	uint32_t mMaxPacketSize;
	std::string mProtocolVersion;
public:
	StyxTVersionMessage(uint32_t maxPacketSize, std::string protocolVersion);
	~StyxTVersionMessage();
	virtual void load(IStyxDataReader*);
};

#endif /* STYXTVERSIONMESSAGE_H_ */
