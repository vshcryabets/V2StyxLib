/*
 * StyxMessage.cpp
 *
 *  Created on: May 24, 2012
 *      Author: vschryabets@gmail.com
 */

#include "stdio.h"
#include "types.h"
#include "messages/base/StyxMessage.h"
#include "messages/StyxTVersionMessage.h"
#include "messages/StyxRVersionMessage.h"
#include "messages/StyxTAttachMessage.h"
#include "messages/StyxTWalkMessage.h"
#include "messages/StyxTOpenMessage.h"
#include "messages/StyxTReadMessage.h"
#include "messages/StyxTFlushMessage.h"
#include "messages/StyxTWriteMessage.h"
#include "messages/StyxTWStatMessage.h"
#include "io/IStyxDataReader.h"
#include "messages/StyxTCreateMessage.h"
#include "messages/StyxRErrorMessage.h"
#include "messages/StyxRAttachMessage.h"
#include "messages/StyxRWalkMessage.h"
#include "messages/StyxROpenMessage.h"
#include "messages/StyxRReadMessage.h"
#include "messages/StyxRWriteMessage.h"
#include "messages/StyxRStatMessage.h"
#include <sstream>

size_t StyxMessage::getUTFSize(StyxString utf) {
	return utf.length();
}

StyxMessage::StyxMessage(MessageTypeEnum type, StyxTAG tag) {
	mType = type;
	mTag = tag;
}

StyxMessage::~StyxMessage() {
	// TODO Auto-generated destructor stub
}

/**
 * Construct message from DoubleStateBuffer
 * @param is intput stream
 * @param io_unit packet size
 * @return constructed Message object
 * @throws IOException
 */
StyxMessage* StyxMessage::factory(IStyxDataReader* buffer, size_t io_unit) {
	// get common packet data
	size_t packet_size = buffer->readUInt32();
	if ( packet_size > io_unit ) throw "Packet size to large";
	int type = buffer->readUInt8();
	uint16_t tag = buffer->readUInt16();
	// load other data
	StyxMessage *result = NULL;
	switch (type) {
	case Tversion:
		result = new StyxTVersionMessage(0, "");
		break;
	case Rversion:
		result = new StyxRVersionMessage(0, NULL);
		break;
		//	case Tauth:
		//		result = new StyxTAuthMessage(NOFID);
		//		break;
	case Tflush:
		result = new StyxTFlushMessage(NOTAG);
		break;
	case Tattach:
		result = new StyxTAttachMessage(NOFID, NOFID, "", "");
		break;
	case Twalk:
		result = new StyxTWalkMessage(NOFID, NOFID);
		break;
		//	case Rauth:
		//		result = new StyxRAuthMessage(tag, StyxQID.EMPTY);
		//		break;
    case Rerror:
        result = new StyxRErrorMessage(tag, NULL);
        break;
    case Rflush:
        result = new StyxMessage(Rflush, tag);
        break;
    case Rattach:
        result = new StyxRAttachMessage(tag, StyxQID::EMPTY);
        break;
    case Rwalk:
        result = new StyxRWalkMessage(tag, NULL);
        break;
	case Topen:
		result = new StyxTOpenMessage(NOFID, OREAD);
		break;
    case Ropen:
        result = new StyxROpenMessage(tag, NULL, 0, false);
        break;
    case Tcreate:
        result = new StyxTCreateMessage(NOFID, NULL, 0, OWRITE);
        break;
    case Rcreate:
        result = new StyxROpenMessage(tag, NULL, 0, true);
        break;
	case Tread:
		result = new StyxTReadMessage(NOFID, 0, 0);
		break;
    case Rread:
        result = new StyxRReadMessage(tag, NULL, 0);
        break;
	case Twrite:
		result = new StyxTWriteMessage(NOFID, 0, NULL, 0);
		break;
    case Rwrite:
        result = new StyxRWriteMessage(tag, 0);
        break;
	case Tclunk:
		result = new StyxTMessageFID(Tclunk, Rclunk, 0);
		break;
	case Rclunk:
		result = new StyxMessage(Rclunk, tag);
		break;
	case Tremove:
		result = new StyxTMessageFID(Tremove, Rremove, 0);
		break;
	case Rremove:
		result = new StyxMessage(Rremove, tag);
		break;
	case Tstat:
		result = new StyxTMessageFID(Tstat, Rstat, tag);
		break;
	case Rstat:
		result = new StyxRStatMessage(tag);
		break;
	case Twstat:
		result = new StyxTWStatMessage(NOFID, NULL, false);
		break;
	case Rwstat:
		result = new StyxMessage(Rwstat, tag);
		break;
	default:
		throw "Unknown message";
		return NULL;
	}
	result->setTag(tag);
	result->load(buffer);
	return result;
}

void StyxMessage::setTag(uint16_t tag) {
	mTag = tag;
}

MessageTypeEnum StyxMessage::getType() {
	return mType;
}

StyxTAG StyxMessage::getTag() {
	return mTag;
}

void StyxMessage::load(IStyxDataReader *buffer) {
}

void StyxMessage::writeToBuffer(IStyxDataWriter *output) {
	size_t packetSize = getBinarySize();
	output->clear();
	output->limit(packetSize);
	output->writeUInt32(packetSize);
	output->writeUInt8(mType);
	output->writeUInt16(getTag());
}

size_t StyxMessage::getBinarySize()	{
	return StyxMessage::BASE_BINARY_SIZE;
}

std::string StyxMessage::toString() {
	std::stringstream stream;
	stream << "Type " << getType() << " Tag " << getTag();
    throw stream.str();

}
