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

class StyxTMessage : public StyxMessage{
private:
	MessageTypeEnum mRequiredAnswerType;
    StyxMessage* mAnswer;

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
	void setAnswer(StyxMessage* answer);
	// =======================================================
	// Virtual methods
	// =======================================================
	StyxMessage* waitForAnswer(long timeout);
};

#endif /* STYX_TMESSAGE_H_ */
