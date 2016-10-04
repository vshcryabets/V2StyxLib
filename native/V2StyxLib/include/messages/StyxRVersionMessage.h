/*
 * StyxRVersionMessage.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRVERSIONMESSAGE_H_
#define STYXRVERSIONMESSAGE_H_
#include <string>
#include "messages/base/StyxMessage.h"

class StyxRVersionMessage : public StyxMessage {
private:
	size_t mIOUnit;
	std::string mProtocol;
public:
	StyxRVersionMessage(size_t iounit, std::string protocol);
	virtual ~StyxRVersionMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRVERSIONMESSAGE_H_ */
