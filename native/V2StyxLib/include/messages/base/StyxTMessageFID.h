/*
 * StyxTMessage.h
 *
 *  Created on: May 24, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYX_TMESSAGE_FID_H_
#define STYX_TMESSAGE_FID_H_
#include "types.h"
#include "messages/base/StyxTMessage.h"

class StyxTMessageFID : public StyxTMessage {
private:
	StyxFID mFID;
protected:
public:
	StyxTMessageFID(MessageTypeEnum type, MessageTypeEnum answer, StyxFID fid);
	virtual ~StyxTMessageFID();
	// =======================================================
	// Getters
	// =======================================================
	StyxFID getFID();
	// =======================================================
	// Setters
	// =======================================================
	void setFID(StyxFID fid);
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter *outputBuffer);
	virtual size_t getBinarySize();
	virtual StyxString toString();
};

#endif /* STYX_TMESSAGE_FID_H_ */
