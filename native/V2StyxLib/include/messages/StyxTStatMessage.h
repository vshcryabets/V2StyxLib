/*
 * StyxTStatMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXTSTATMESSAGE_H_
#define STYXTSTATMESSAGE_H_

#include "messages/base/StyxMessage.h"

class StyxTStatMessage: public StyxMessage {
private:
	StyxFID mFID;
public:
	StyxTStatMessage(StyxFID fid);
	virtual ~StyxTStatMessage();
	StyxFID getFID();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTSTATMESSAGE_H_ */
