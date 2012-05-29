/*
 * StyxRVersionMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxRVersionMessage.h"

StyxRVersionMessage::StyxRVersionMessage(size_t iounit, std::string *protocol)
	: StyxMessage( Rversion, StyxMessage::NOTAG ) {
	mIOUnit = iounit;
	mProtocol = protocol;
}

StyxRVersionMessage::~StyxRVersionMessage() {
	// TODO Auto-generated destructor stub
}

void StyxRVersionMessage::load(StyxBufferOperations *buffer) {
    mIOUnit  = buffer->readUInt32();
    mProtocol = &(buffer->readUTF()); // TODO this is wrong, memory leak
}
