/*
 * StyxByteBufferWritable.h
 *
 *  Created on: May 31, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXBYTEBUFFERWRITABLE_H_
#define STYXBYTEBUFFERWRITABLE_H_
#include "StyxDataWriter.h"

class StyxByteBufferWritable : public StyxDataWriter {
private:
	uint8_t *mBuffer;
	size_t mPosition, mLimit;
	size_t mCapacity;
public:
	StyxByteBufferWritable(int capacity);
	~StyxByteBufferWritable();
	uint8_t* getBuffer();
	size_t getCapacity();
	// ==================================================
	// IStyxDataWriter methods
	// ==================================================
	virtual size_t write(const uint8_t* data, size_t offset, size_t count);
	virtual void clear();
	virtual void limit(size_t limit);
	virtual size_t getPosition();
};

#endif /* STYXBYTEBUFFERWRITABLE_H_ */
