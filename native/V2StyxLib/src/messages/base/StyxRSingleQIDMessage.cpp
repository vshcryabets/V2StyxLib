/*
 * StyxRSingleQIDMessage.cpp
 *
 *  Created on: Apr 07, 2018
 *      Author: vschryabets@gmail.com
 */

#include "messages/base/StyxRSingleQIDMessage.h"
#include <sstream>

StyxRSingleQIDMessage::StyxRSingleQIDMessage(MessageTypeEnum type, StyxTAG tag, const StyxQID qid) :
	StyxMessage(type, tag), mQID(qid) {
}

StyxRSingleQIDMessage::~StyxRSingleQIDMessage() {
}

void StyxRSingleQIDMessage::load(IStyxDataReader *buffer) {
	StyxMessage::load(buffer);
	mQID = StyxQID(buffer);
}

void StyxRSingleQIDMessage::writeToBuffer(IStyxDataWriter *outputBuffer) {
	StyxMessage::writeToBuffer(outputBuffer);
	mQID.writeBinaryTo(outputBuffer);
}

size_t StyxRSingleQIDMessage::getBinarySize() {
	return StyxMessage::getBinarySize() + StyxQID::CONTENT_SIZE;
}

StyxQID StyxRSingleQIDMessage::getQID() {
	return mQID;
}

StyxString StyxRSingleQIDMessage::toString() {
	std::stringstream stream;
	stream << StyxMessage::toString() << " QID: "
			<< mQID.toString();
	return stream.str();

}
