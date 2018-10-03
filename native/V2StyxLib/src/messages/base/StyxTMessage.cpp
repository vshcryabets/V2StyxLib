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
	  mRequiredAnswerType(answer), mWaitSyncObject(NULL), mAnswer(NULL)
{
}

StyxTMessage::~StyxTMessage() {
}

StyxMessage* StyxTMessage::getAnswer() {
	return mAnswer;
}

void StyxTMessage::setAnswer(StyxMessage* answer) throw(StyxException) {
	if (!checkAnswer(answer))
		throw StyxWrongMessageException(answer, mRequiredAnswerType);
#warning set answer should be after mutex locked
	mAnswer = answer;
	if (mWaitSyncObject != NULL) {
		mWaitSyncObject->notifyAll();
	}
}

StyxMessage* StyxTMessage::waitForAnswer() 
	throw(StyxErrorMessageException) 
{
	mWaitSyncObject->waitForNotify(&mAnswer);
	if ( mAnswer == NULL )
		throw StyxException("Don't receive answer for %s", this->toString().c_str());
	if (mAnswer->getType() == Rerror) {
		StyxErrorMessageException::checkException(mAnswer);
	}
	return mAnswer;
}

#warning do we need this method?
bool StyxTMessage::checkAnswer(StyxMessage* answer) 
{
	MessageTypeEnum received = answer->getType();
	return (mRequiredAnswerType == received || received == Rerror);
}

void StyxTMessage::setSyncObject(SyncObject* syncObject)
{
	mWaitSyncObject = syncObject;
}
