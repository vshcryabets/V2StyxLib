/*
 * StyxErrorMessageException.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "exceptions/StyxErrorMessageException.h"

StyxErrorMessageException::StyxErrorMessageException(StyxRErrorMessage *message) {
	mMessage = message;
}

StyxErrorMessageException::StyxErrorMessageException(const char *message) {
	mMessage = new StyxRErrorMessage(StyxMessage::NOTAG, message);
}

StyxErrorMessageException::~StyxErrorMessageException() {
	// TODO Auto-generated destructor stub
}

StyxRErrorMessage* StyxErrorMessageException::getErrorMessage() {
	return mMessage;
}
