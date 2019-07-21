/*
 * StyxTVersionMessage.cpp
 *
 *  Created on: May 24, 2012
 *      Author: mrco
 */

#include "messages/StyxTVersionMessage.h"
#include <sstream>

StyxTVersionMessage::StyxTVersionMessage(uint32_t maxPacketSize, std::string protocolVersion)
	: StyxTMessage(Tversion, Rversion) {
	mMaxPacketSize = maxPacketSize;
	mProtocolVersion = protocolVersion;
}

StyxTVersionMessage::~StyxTVersionMessage() {
}

void StyxTVersionMessage::writeToBuffer(IStyxDataWriter* output) 
{
	StyxTMessage::writeToBuffer(output);
	output->writeUInt32(mMaxPacketSize);
	output->writeUTFString(mProtocolVersion);
}

void StyxTVersionMessage::load(IStyxDataReader* input) {
	mMaxPacketSize = input->readUInt32();
	mProtocolVersion = input->readUTFString();
}

size_t StyxTVersionMessage::getBinarySize() 
{
	return StyxTMessage::getBinarySize() + sizeof(mMaxPacketSize)
			+ StyxMessage::getUTFSize(mProtocolVersion);
}

StyxString StyxTVersionMessage::toString() 
{
	std::stringstream stream;
	stream << StyxMessage::toString() << " IOUnit " << mMaxPacketSize << " ProtoName " << mProtocolVersion;
    return stream.str();
}