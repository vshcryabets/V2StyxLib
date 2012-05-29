/*
 * StyxMessage.h
 *
 *  Created on: May 24, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXMESSAGE_H_
#define STYXMESSAGE_H_
#include "../types.h"
#include "../io/StyxBufferOperations.h"

class StyxMessage {
private:
	uint16_t mTag;
	MessageTypeEnum mType;

public:
	static const int NOTAG = 0xFFFF;
	static const size_t BASE_BINARY_SIZE = 7;

	static StyxMessage* factory(StyxBufferOperations *buffer, size_t iounit);
	StyxMessage(MessageTypeEnum type, uint16_t tag);
	virtual ~StyxMessage();
	// =======================================================
	// Getters
	// =======================================================
	MessageTypeEnum getType();
	uint16_t getTag();
	size_t getBinarySize();
	// =======================================================
	// Setters
	// =======================================================
	void setTag(uint16_t tag);
	// =======================================================
	// Virtual methods
	// =======================================================
	virtual void load(StyxBufferOperations *buffer) = 0;
	virtual size_t writeToBuffer(StyxBufferOperations *outputBuffer, size_t ioUnit);
};

#endif /* STYXMESSAGE_H_ */
