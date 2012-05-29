/*
 * DualStateBuffer.h
 *
 *  Created on: May 22, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef DUALSTATEBUFFER_H_
#define DUALSTATEBUFFER_H_
#include "../types.h"
#include "IStyxDataReader.h"

class StyxByteBufferReadable : public StyxDataReader {
private:
	uint8_t *mBuffer;
	size_t mWritePosition, mReadPosition, mCapacity, mStoredBytes;
public:
	StyxByteBufferReadable(size_t capacity);
	virtual ~DualStateBuffer();
	size_t remainsToRead();
	size_t readFromFD(Socket fd);
	/**
	 * Get byte array from buffer, this operation will not move read position pointer
	 * @param out
	 * @param i
	 * @param length
	 */
	size_t get(uint8_t[] out, size_t i, size_t length);
	// =========================================================
	// Virtual methods
	// =========================================================
	/**
	 * Read byte array from buffer
	 * @param out
	 * @param i
	 * @param length
	 */
	virtual size_t read(uint8_t[] out, size_t i, size_t length);

	@Override
protected long getInteger(int bytes) {
		// TODO this method will work wrong at the buffer end
		if ( bytes > sDataBufferSize ) throw new ArrayIndexOutOfBoundsException("Too much bytes to read");
		long result = 0L;
		int shift = 0;
		int readed = get(mDataBuffer, 0, bytes);
		if ( readed != bytes ) throw new ArrayIndexOutOfBoundsException("Can't read bytes");
		for (int i=0; i<bytes; i++) {
			long b = (mDataBuffer[i]&0xFF);
			if (shift > 0)
				b <<= shift;
			shift += 8;
			result |= b;
		}
		return result;
	}

};

#endif /* DUALSTATEBUFFER_H_ */
