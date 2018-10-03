/*
 * StyxRVersionMessage.cpp
 *
 *  Created on: May 27, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/StyxRVersionMessage.h"

StyxRVersionMessage::StyxRVersionMessage(size_t iounit, StyxString protocol)
: StyxMessage( Rversion, StyxMessage::NOTAG ) {
	mIOUnit = iounit;
	mProtocol = protocol;
}

StyxRVersionMessage::~StyxRVersionMessage() {
	// TODO Auto-generated destructor stub
}

void StyxRVersionMessage::load(IStyxDataReader *buffer) {
	mIOUnit  = buffer->readUInt32();
	mProtocol = buffer->readUTFString();
}

void StyxRVersionMessage::writeToBuffer(IStyxDataWriter* output) {
	StyxMessage::writeToBuffer(output);
	output->writeUInt32(mIOUnit);
	output->writeUTFString(mProtocol);
}

size_t StyxRVersionMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + sizeof(mIOUnit)
			+ StyxMessage::getUTFSize(mProtocol);
}

size_t StyxRVersionMessage::getMaxPacketSize() {
	return mIOUnit;
}
