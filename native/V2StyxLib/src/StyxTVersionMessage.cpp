/*
 * StyxTVersionMessage.cpp
 *
 *  Created on: May 24, 2012
 *      Author: mrco
 */

#include "StyxTVersionMessage.h"
#include "types.h"

StyxTVersionMessage::StyxTVersionMessage(uint32_t maxPacketSize, std::string protocolVersion):
	StyxMessage( Tversion, StyxMessage::NOTAG ) {
	mMaxPacketSize = maxPacketSize;
	mProtocolVersion = protocolVersion;

}

StyxTVersionMessage::~StyxTVersionMessage() {
	// TODO Auto-generated destructor stub
}

void StyxTVersionMessage::load(StyxBufferOperations* input) {
	mMaxPacketSize = input->readUInt32();
	mProtocolVersion = input->readUTF();
}

