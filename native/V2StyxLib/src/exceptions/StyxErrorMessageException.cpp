/*
 * StyxErrorMessageException.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "exceptions/StyxErrorMessageException.h"

StyxErrorMessageException::StyxErrorMessageException(StyxRErrorMessage *message) 
	: StyxException(message->getError().c_str()) {
}

StyxErrorMessageException::StyxErrorMessageException(const char *message, ...) 
	: StyxException("") {
	va_list va;
	va_start(va, message);
	setMessage(message, va);
	va_end(va);
}

StyxErrorMessageException::~StyxErrorMessageException() {
	// TODO Auto-generated destructor stub
}

StyxRErrorMessage* StyxErrorMessageException::constructErrorMessage() {
	StyxRErrorMessage* message = new StyxRErrorMessage(StyxMessage::NOTAG, getMessage());
	return message;
}

void StyxErrorMessageException::checkException(StyxMessage *rMessage)  throw(StyxException) {
	if (rMessage == NULL)
		throw StyxException("rMessage is NULL");
	if (rMessage->getType() != Rerror)
		return;
	StyxRErrorMessage *rError = (StyxRErrorMessage*) rMessage;
	throw StyxErrorMessageException(rError);
}
