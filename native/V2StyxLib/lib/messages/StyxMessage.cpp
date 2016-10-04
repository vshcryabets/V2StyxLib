/*
 * StyxMessage.cpp
 *
 *  Created on: May 24, 2012
 *      Author: vschryabets@gmail.com
 */

#include "messages/base/StyxMessage.h"
#include "messages/StyxTVersionMessage.h"
#include "messages/StyxRVersionMessage.h"
#include "messages/StyxTAttachMessage.h"
#include "messages/StyxTWalkMessage.h"
#include "messages/StyxTStatMessage.h"
#include "messages/StyxTOpenMessage.h"
#include "messages/StyxTReadMessage.h"
#include "messages/StyxTFlushMessage.h"
#include "messages/StyxTWriteMessage.h"
#include "messages/StyxTWStatMessage.h"
#include "io/IStyxDataReader.h"
#include "stdio.h"
#include "../../include/messages/StyxTCreateMessage.h"

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
		//	case Rerror:
		//		result = new StyxRErrorMessage(tag, null);
		//		break;
		//	case Rflush:
		//		result = new StyxRFlushMessage(tag);
		//		break;
		//	case Rattach:
		//		result = new StyxRAttachMessage(tag, StyxQID.EMPTY);
		//		break;
		//	case Rwalk:
		//		result = new StyxRWalkMessage(tag, null);
		//		break;
	case Topen:
		result = new StyxTOpenMessage(NOFID, OREAD);
		break;
		//	case Ropen:
		//		result = new StyxROpenMessage(tag, null, 0);
		//		break;
		//	case Tcreate:
		//		result = new StyxTCreateMessage(NOFID, null, 0, ModeType.OWRITE);
		//		break;
		//	case Rcreate:
		//		result = new StyxRCreateMessage(tag);
		//		break;
	case Tread:
		result = new StyxTReadMessage(NOFID, 0, 0);
		break;
		//	case Rread:
		//		result = new StyxRReadMessage(tag, null, 0);
		//		break;
	case Twrite:
		result = new StyxTWriteMessage( );
		break;
		//	case Rwrite:
		//		result = new StyxRWriteMessage(tag, 0);
		//		break;
	case Tclunk:
		result = new StyxTCreateMessage(tag);
		break;
		//	case Rclunk:
		//		result = new StyxRClunkMessage(tag);
		//		break;
		//	case Tremove:
		//		result = new StyxTRemoveMessage(tag);
		//		break;
		//	case Rremove:
		//		result = new StyxRRemoveMessage(tag);
		//		break;
	case Tstat:
		result = new StyxTStatMessage(tag);
		break;
		//	case Rstat:
		//		result = new StyxRStatMessage(tag);
		//		break;
	case Twstat:
		result = new StyxTWStatMessage(NOFID, NULL);
		break;
		//	case Rwstat:
		//		result = new StyxRWStatMessage(tag);
		//		break;
		//	}
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

size_t StyxMessage::writeToBuffer(IStyxDataWriter *output) {
	size_t packetSize = getBinarySize();
	output->clear();
	output->limit(packetSize);
	output->writeUInt32(packetSize);
	output->writeUInt8(mType);
	output->writeUInt16(getTag());
	return getBinarySize();
}

size_t StyxMessage::getBinarySize()	{
	return StyxMessage::BASE_BINARY_SIZE;
}
