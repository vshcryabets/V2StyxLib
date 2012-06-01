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

void StyxRVersionMessage::load(IStyxDataReader *buffer) {
	mIOUnit  = buffer->readUInt32();
	mProtocol = &(buffer->readUTFString()); // TODO this is wrong, memory leak
}

size_t StyxRVersionMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt32(mIOUnit);
	output->writeUTFString(mProtocol);
	return getBinarySize();
}

size_t StyxRVersionMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + sizeof(uint32_t)
			+ sizeof(uint16_t)+mProtocol->length();
}
