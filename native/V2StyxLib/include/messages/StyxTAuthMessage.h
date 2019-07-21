/*
 * StyxTAuthMessage.h
 *
 */

#ifndef STYXTAUTHMESSAGE_H_
#define STYXTAUTHMESSAGE_H_
#include "messages/base/StyxTMessageFID.h"

class StyxTAuthMessage : public StyxTMessageFID {
private:
	StyxString mUserName;
	StyxString mMountPoint;
public:
	StyxTAuthMessage(StyxFID fid);
	virtual ~StyxTAuthMessage();
	StyxString getMountPoint();
	StyxString getUserName();
	void setUserName(StyxString userName);
	void setMountPoint(StyxString mountPoint);
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
	virtual StyxString toString();
};

#endif /* STYXTAUTHMESSAGE_H_ */
