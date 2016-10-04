/*
 * StyxTAttachMessage.h
 *
 *  Created on: Jun 1, 2012
 *      Author: mrco
 */

#ifndef STYXTATTACHMESSAGE_H_
#define STYXTATTACHMESSAGE_H_
#include "messages/base/StyxTMessageFID.h"
#include <string>

class StyxTAttachMessage : public StyxTMessageFID {
private:
	uint32_t mAuthFID;
	StyxString mUserName;
	StyxString mMountPoint;
public:
	StyxTAttachMessage(uint32_t fid, uint32_t afid, StyxString username, StyxString mountpoint);
	virtual ~StyxTAttachMessage();
	StyxString getMountPoint();
	StyxString getUserName();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTATTACHMESSAGE_H_ */
