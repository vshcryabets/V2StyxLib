/*
 * DualStateBuffer.h
 *
 *  Created on: May 22, 2012
 *      Author: mrco
 */

#ifndef DUALSTATEBUFFER_H_
#define DUALSTATEBUFFER_H_
#include "types.h"
#include "StyxBufferOperations.h"

class DualStateBuffer : public StyxBufferOperations {
private:
	int8_t  *mDataBuffer;
	size_t mWritePosition, mReadPosition, mCapacity, mStoredBytes;
	size_t get(int8_t *out, size_t offset, size_t length);
public:
	DualStateBuffer(size_t size);
	virtual ~DualStateBuffer();
	size_t remainsToRead();
	size_t readFromFD(Socket fd);
	// =========================================================
	// Virtual methods
	// =========================================================
	virtual uint32_t getInteger(int bytes);
	virtual void clear();
	virtual void limit(size_t value);
	virtual void write(int8_t* data);
	virtual void write(int8_t* data, size_t offset, size_t count);
	virtual size_t read(int8_t* data, size_t offset, size_t count);
};

#endif /* DUALSTATEBUFFER_H_ */
