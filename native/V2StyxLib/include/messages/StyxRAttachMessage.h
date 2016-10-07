/*
 * StyxRVersionMessage.h
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXRATTACHMESSAGE_H_
#define STYXRATTACHMESSAGE_H_
#include <string>
#include "messages/base/structs/StyxQID.h"
#include "./messages/base/StyxRSingleQIDMessage.h"

class StyxRAttachMessage : public StyxRSingleQIDMessage {
public:
	StyxRAttachMessage(int tag, const StyxQID *qid);
	virtual ~StyxRAttachMessage();
};

#endif /* STYXRVERSIONMESSAGE_H_ */
