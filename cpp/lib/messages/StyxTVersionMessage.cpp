/*
 * StyxTVersionMessage.cpp
 *
 *  Created on: May 24, 2012
 *      Author: mrco
 */

#include "messages/StyxTVersionMessage.h"

StyxTVersionMessage::StyxTVersionMessage(uint32_t maxPacketSize, std::string protocolVersion):
	StyxMessage( Tversion, StyxMessage::NOTAG ) {
	mMaxPacketSize = maxPacketSize;
	mProtocolVersion = protocolVersion;

}

StyxTVersionMessage::~StyxTVersionMessage() {
	// TODO Auto-generated destructor stub
}

void StyxTVersionMessage::load(IStyxDataReader* input) {
	mMaxPacketSize = input->readUInt32();
	mProtocolVersion = input->readUTFString();
}

