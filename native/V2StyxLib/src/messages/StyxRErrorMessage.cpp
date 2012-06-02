/*
 * StyxRErrorMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: mrco
 */

#include "StyxRErrorMessage.h"

StyxRAttachMessage::StyxRAttachMessage(int tag, std::string *message)
	: StyxMessage( Rerror, tag) {
	mMessage = message;
}

StyxRAttachMessage::~StyxRAttachMessage() {
	// TODO Auto-generated destructor stub
}

