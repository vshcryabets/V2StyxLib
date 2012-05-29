/*
 * StyxRVersionMessage.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRVERSIONMESSAGE_H_
#define STYXRVERSIONMESSAGE_H_
#include <string>
#include "StyxMessage.h"

class StyxRVersionMessage : public StyxMessage {
private:
	size_t mIOUnit;
	std::string *mProtocol;
public:
	StyxRVersionMessage(size_t iounit, std::string *protocol);
	virtual ~StyxRVersionMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
};

#endif /* STYXRVERSIONMESSAGE_H_ */
