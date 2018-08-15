/*
 * DualStateBuffer.h
 *
 *  Created on: May 22, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef StyxByteBufferReadable_H_
#define StyxByteBufferReadable_H_
#include "types.h"
#include "io/IStyxBuffer.h"
#include "exceptions/StyxException.h"

class StyxByteBufferReadable : public IStyxBuffer {
private:
	uint8_t *mBuffer;
	size_t mWritePosition, mReadPosition;
	size_t mCapacity, mStoredBytes;
	size_t mCurrentLimit;

	size_t updateBufferLimits();
	void moveWritePointer(size_t read);
public:
	StyxByteBufferReadable(size_t capacity);
	virtual ~StyxByteBufferReadable();
	size_t remainsToRead();
	size_t readFromChannel(Socket fd) throw(StyxException);
	/**
	 * Get byte array from buffer, this operation will not move read position pointer
	 * @param out
	 * @param i
	 * @param length
	 */
	size_t get(uint8_t *out, size_t i, size_t length);
	// =========================================================
	// Virtual methods
	// =========================================================
	virtual size_t write(uint8_t *buffer, size_t length);
	virtual uint8_t *getBuffer();
	virtual void clear();
	virtual size_t get(uint8_t *out, size_t length);
	virtual void moveReadPointerBy(size_t bytes);
	virtual size_t read(uint8_t *out, size_t length);
};

#endif /* DUALSTATEBUFFER_H_ */
