/*
 * StyxTAttachMessage.h
 *
 *  Created on: Jun 1, 2012
 *      Author: mrco
 */

#ifndef STYXTATTACHMESSAGE_H_
#define STYXTATTACHMESSAGE_H_
#include "StyxMessage.h"
#include <string>

class StyxTAttachMessage : public StyxMessage {
private:
	uint32_t mFID, mAuthFID;
	std::string mUserName;
	std::string mMountPoint;
public:
	StyxTAttachMessage(uint32_t fid, uint32_t afid, std::string username, std::string mountpoint);
	virtual ~StyxTAttachMessage();
	std::string getMountPoint();
	std::string getUserName();
	uint32_t getFID();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTATTACHMESSAGE_H_ */
