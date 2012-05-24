/*
 * StyxMessage.cpp
 *
 *  Created on: May 24, 2012
 *      Author: vschryabets@gmail.com
 */

#include "StyxMessage.h"
#include "StyxTVersionMessage.h"

StyxMessage::StyxMessage(MessageTypeEnum type, uint16_t tag) :
mType(type), mTag(tag) {
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
StyxMessage* StyxMessage::factory(StyxBufferOperations* buffer, size_t io_unit) {
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
		//	case Rversion:
		//		result = new StyxRVersionMessage(0, null);
		//		break;
		//	case Tauth:
		//		result = new StyxTAuthMessage(NOFID);
		//		break;
		//	case Tflush:
		//		result = new StyxTFlushMessage(NOTAG);
		//		break;
		//	case Tattach:
		//		result = new StyxTAttachMessage(NOFID, NOFID, null, null);
		//		break;
		//	case Twalk:
		//		result = new StyxTWalkMessage(NOFID, NOFID, "");
		//		break;
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
		//	case Topen:
		//		result = new StyxTOpenMessage(NOFID, ModeType.OREAD);
		//		break;
		//	case Ropen:
		//		result = new StyxROpenMessage(tag, null, 0);
		//		break;
		//	case Tcreate:
		//		result = new StyxTCreateMessage(NOFID, null, 0, ModeType.OWRITE);
		//		break;
		//	case Rcreate:
		//		result = new StyxRCreateMessage(tag);
		//		break;
		//	case Tread:
		//		result = new StyxTReadMessage(NOFID, null, 0);
		//		break;
		//	case Rread:
		//		result = new StyxRReadMessage(tag, null, 0);
		//		break;
		//	case Twrite:
		//		result = new StyxTWriteMessage(NOFID, null, null );
		//		break;
		//	case Rwrite:
		//		result = new StyxRWriteMessage(tag, 0);
		//		break;
		//	case Tclunk:
		//		result = new StyxTClunkMessage(tag);
		//		break;
		//	case Rclunk:
		//		result = new StyxRClunkMessage(tag);
		//		break;
		//	case Tremove:
		//		result = new StyxTRemoveMessage(tag);
		//		break;
		//	case Rremove:
		//		result = new StyxRRemoveMessage(tag);
		//		break;
		//	case Tstat:
		//		result = new StyxTStatMessage(tag);
		//		break;
		//	case Rstat:
		//		result = new StyxRStatMessage(tag);
		//		break;
		//	case Twstat:
		//		result = new StyxTWStatMessage(NOFID, null);
		//		break;
		//	case Rwstat:
		//		result = new StyxRWStatMessage(tag);
		//		break;
		//	}
	}
	result->setTag(tag);
	result->load(buffer);
	return result;
}

void StyxMessage::setTag(uint16_t tag) {
	mTag = tag;
}
