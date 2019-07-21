/*
 * StyxTMessageFID.cpp
 *
 *  Created on: Apr 07, 2018
 *      Author: vschryabets@gmail.com
 */

#include "messages/base/StyxTMessageFID.h"
#include <sstream>

StyxTMessageFID::StyxTMessageFID(MessageTypeEnum type, MessageTypeEnum answer, StyxFID fid)
	: StyxTMessage(type, answer), mFID(fid) {
}

StyxTMessageFID::~StyxTMessageFID() {
}

StyxFID StyxTMessageFID::getFID() {
	return mFID;
}

void StyxTMessageFID::setFID(StyxFID fid) {
	mFID = fid;
}

void StyxTMessageFID::load(IStyxDataReader *buffer) {
	StyxTMessage::load(buffer);
	mFID = buffer->readUInt32();
}

void StyxTMessageFID::writeToBuffer(IStyxDataWriter *outputBuffer) {
	StyxTMessage::writeToBuffer(outputBuffer);
	outputBuffer->writeUInt32(mFID);
}

size_t StyxTMessageFID::getBinarySize() {
	return StyxTMessage::getBinarySize() + sizeof(StyxFID);
}

StyxString StyxTMessageFID::toString() {
	StyxString result = StyxTMessage::toString();
	std::stringstream stream;
	stream << result << "\tFID: " << mFID;
	return stream.str();
}