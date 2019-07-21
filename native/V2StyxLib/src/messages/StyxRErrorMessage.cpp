/*
 * StyxRErrorMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxRErrorMessage.h"
#include "string.h"

StyxRErrorMessage::StyxRErrorMessage(StyxTAG tag, StyxString message)
	: StyxMessage( Rerror, tag), mMessage(message) {
}
StyxRErrorMessage::StyxRErrorMessage(StyxTAG tag, const char *message)
	: StyxMessage( Rerror, tag), mMessage(message) {
}

StyxRErrorMessage::~StyxRErrorMessage() {
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRErrorMessage::load(IStyxDataReader *buffer) {
	mMessage = buffer->readUTFString();
}

void StyxRErrorMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUTFString(mMessage);
}
size_t StyxRErrorMessage::getBinarySize() {
	return StyxMessage::getBinarySize()
		+ mMessage.size()+2;
}

StyxString StyxRErrorMessage::getError() {
	return mMessage;
}