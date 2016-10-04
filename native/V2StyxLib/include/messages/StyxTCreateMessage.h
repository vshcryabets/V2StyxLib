/*
 * StyxTClunkMessage.h
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#ifndef STYXTCLUNKMESSAGE_H_
#define STYXTCLUNKMESSAGE_H_

#include "messages/base/StyxTMessageFID.h"

class StyxTCreateMessage: public StyxTMessageFID {
private:
	StyxString mName;
	uint16_t mMode;
	uint32_t mPermissions;
public:
	StyxTCreateMessage(StyxFID fid, StyxString name, uint32_t permissions, uint16_t mode);
	virtual ~StyxTCreateMessage();
	StyxFID getFID();

	StyxString getName();
	void setName(StyxString name);
	uint32_t getPermissions();
	void setPermissions(uint32_t permissions);
	uint16_t getMode();
	void setMode(uint16_t mode);
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader* input);
	virtual void writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXTCLUNKMESSAGE_H_ */