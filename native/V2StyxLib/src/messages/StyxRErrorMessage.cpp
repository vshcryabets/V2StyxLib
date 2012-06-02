/*
 * StyxRErrorMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: mrco
 */

#include "StyxRErrorMessage.h"
#include "string.h"

StyxRErrorMessage::StyxRErrorMessage(StyxTAG tag, std::string message)
	: StyxMessage( Rerror, tag) {
	StyxRErrorMessage(tag, message.c_str());
}
StyxRErrorMessage::StyxRErrorMessage(StyxTAG tag, const char *message)
	: StyxMessage( Rerror, tag) {
	size_t size = strlen(message);
	mMessage = new char[size+1];
	strncpy(mMessage, message, size);
}

StyxRErrorMessage::~StyxRErrorMessage() {
	delete [] mMessage;
}

