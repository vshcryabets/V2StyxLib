/*
 * StyxRErrorMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxRErrorMessage.h"
#include "string.h"

StyxRErrorMessage::StyxRErrorMessage(StyxTAG tag, StyxString message)
	: StyxMessage( Rerror, tag) {
	mMessage = new StyxString(message);
}
StyxRErrorMessage::StyxRErrorMessage(StyxTAG tag, const char *message)
	: StyxMessage( Rerror, tag) {
	mMessage = new StyxString(message);
}

StyxRErrorMessage::~StyxRErrorMessage() {
	delete mMessage;
}
// =======================================================
// Virtual methods
// =======================================================
void StyxRErrorMessage::load(IStyxDataReader *buffer) {
	mMessage = new StyxString(buffer->readUTFString());
}
size_t StyxRErrorMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUTFString(mMessage);
	return getBinarySize();
}
size_t StyxRErrorMessage::getBinarySize() {
	return StyxMessage::getBinarySize()
		+ mMessage->size()+2;
}
