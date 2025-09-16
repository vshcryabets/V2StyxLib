/*
 * StyxRVersionMessage.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRATTACHMESSAGE_H_
#define STYXRATTACHMESSAGE_H_
#include <string>
#include "../structs/StyxQID.h"
#include "StyxMessage.h"

class StyxRAttachMessage : public StyxMessage {
private:
	StyxQID *mQID;
public:
	StyxRAttachMessage(int tag, StyxQID *qid);
	virtual ~StyxRAttachMessage();
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *input);
	virtual size_t writeToBuffer(IStyxDataWriter* output);
	virtual size_t getBinarySize();
};

#endif /* STYXRVERSIONMESSAGE_H_ */
