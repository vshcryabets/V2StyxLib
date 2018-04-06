/*
 * StyxTMessage.cpp
 *
 *  Created on: Apr 06, 2018
 *      Author: vschryabets@gmail.com
 */

#include "messages/base/StyxTMessage.h"
#include "exceptions/StyxWrongMessageException.h"

StyxTMessage::StyxTMessage(MessageTypeEnum type, MessageTypeEnum answer)
	: StyxMessage(type, StyxMessage::NOTAG),
	  mRequiredAnswerType(answer){
	pthread_cond_init(&mWaitCondition, NULL);
}

StyxTMessage::~StyxTMessage() {
	pthread_cond_destroy(&mWaitCondition);
}

StyxMessage* StyxTMessage::getAnswer() {
	return mAnswer;
}

void StyxTMessage::setAnswer(StyxMessage* answer) throw(StyxException) {
	if (!checkAnswer(answer))
		throw StyxWrongMessageException(answer, mRequiredAnswerType);
	mAnswer = answer;
	pthread_cond_signal(&mWaitCondition);
}

StyxMessage* StyxTMessage::waitForAnswer(uint32_t timeout) throw(StyxErrorMessageException) {
	if ( mAnswer == NULL) {
		struct timespec tm;
		tm.tv_sec = timeout / 1000;
		tm.tv_nsec = (timeout - tm.tv_sec * 1000) * 1000000;
		pthread_cond_timedwait(&mWaitCondition, NULL, &tm);
	}
	if ( mAnswer == NULL )
		throw new StyxException(std::string("Don't receive answer for ") + this->toString());
	if (mAnswer->getType() == Rerror) {
		StyxErrorMessageException::checkException(mAnswer);
	}
	return mAnswer;
}

bool StyxTMessage::checkAnswer(StyxMessage* answer) {
	MessageTypeEnum received = answer->getType();
	return (mRequiredAnswerType == received || received == Rerror);
}

