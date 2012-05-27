/*
 * StyxByteBuffer.h
 *
 *  Created on: May 27, 2012
 *      Author: mrco
 */

#ifndef STYXBYTEBUFFER_H_
#define STYXBYTEBUFFER_H_
#include "StyxBufferOperations.h"

class StyxByteBuffer : public StyxBufferOperations {
private:
	char* mBuffer;
public:
	StyxByteBuffer(const char* buffer);
	virtual ~StyxByteBuffer();
};

#endif /* STYXBYTEBUFFER_H_ */
