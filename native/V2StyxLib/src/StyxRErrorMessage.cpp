/*
 * StyxRErrorMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: mrco
 */

#include "StyxRErrorMessage.h"

StyxRErrorMessage::StyxRErrorMessage(int tag, std::string *message)
	: StyxMessage( Rerror, tag) {
	mMessage = message;
}

StyxRErrorMessage::~StyxRErrorMessage() {
	// TODO Auto-generated destructor stub
}

