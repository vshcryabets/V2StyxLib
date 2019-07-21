/*
 * StyxTMessage.h
 *
 *  Created on: May 24, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYX_TMESSAGE_H_
#define STYX_TMESSAGE_H_
#include "types.h"
#include "messages/base/StyxMessage.h"
#include "exceptions/StyxException.h"
#include "exceptions/StyxErrorMessageException.h"
#include "utils/SyncObject.h"

class StyxTMessage : public StyxMessage {
private:
	MessageTypeEnum mRequiredAnswerType;
    StyxMessage* mAnswer;
    SyncObject* mWaitSyncObject;
protected:
    bool checkAnswer(StyxMessage *answer);
public:
    StyxTMessage(MessageTypeEnum type, MessageTypeEnum answer);
	virtual ~StyxTMessage();
	// =======================================================
	// Getters
	// =======================================================
	StyxMessage* getAnswer();
	// =======================================================
	// Setters
	// =======================================================
	void setAnswer(StyxMessage* answer) throw(StyxException);
#warning we can move sync logic out of this class. For example we can have map in the channel driver.
	void setSyncObject(SyncObject* syncObject);
	StyxMessage* waitForAnswer() throw(StyxErrorMessageException);
};

#endif /* STYX_TMESSAGE_H_ */
