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
#include <pthread.h>

class StyxTMessage : public StyxMessage {
private:
	MessageTypeEnum mRequiredAnswerType;
    StyxMessage* mAnswer;
    pthread_cond_t mWaitCondition;

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
	// =======================================================
	// Virtual methods
	// =======================================================
	/**
	 * timeout in milliseconds.
	 */
	StyxMessage* waitForAnswer(uint32_t timeout) throw(StyxErrorMessageException);
};

#endif /* STYX_TMESSAGE_H_ */
