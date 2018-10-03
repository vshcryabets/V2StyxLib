/*
 * StyxMessage.h
 *
 *  Created on: May 24, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXMESSAGE_H_
#define STYXMESSAGE_H_
#include "types.h"
#include "io/IStyxDataReader.h"
#include "io/IStyxDataWriter.h"
#include "exceptions/StyxException.h"

class StyxMessage {
private:
	StyxTAG mTag;
	MessageTypeEnum mType;

public:
	static const StyxTAG NOTAG = 0xFFFF;
	static const StyxFID NOFID = 0xFFFFFFFFL;
	static const size_t BASE_BINARY_SIZE = 7;

	static size_t getUTFSize(StyxString utf);
	static StyxMessage* factory(IStyxDataReader *buffer, size_t iounit) throw(StyxException);

	StyxMessage(MessageTypeEnum type, StyxTAG tag);
	virtual ~StyxMessage();
	// =======================================================
	// Getters
	// =======================================================
	MessageTypeEnum getType();
	StyxTAG getTag();
	// =======================================================
	// Setters
	// =======================================================
	void setTag(StyxTAG tag);
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(IStyxDataReader *buffer);
	virtual void writeToBuffer(IStyxDataWriter *outputBuffer);
	virtual size_t getBinarySize();
	virtual StyxString toString();
};

#endif /* STYXMESSAGE_H_ */
