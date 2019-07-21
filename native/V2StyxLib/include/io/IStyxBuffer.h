/*
 * IStyxBuffer.h
 *  Styx byte buffer interface
 *  Created on: May 4, 2014
 *      Author: vschryabets@gmail.com
 */
#ifndef IStyxBuffer_H_
#define IStyxBuffer_H_
#include <stdint.h>

class IStyxBuffer {
public:
	virtual ~IStyxBuffer() {};

    /**
     * Read byte array from buffer.
     * @param out output buffer.
     * @param length
     */
    virtual size_t read(uint8_t *out, size_t length) = 0;

    /**
     * Get byte array from buffer, this operation will not move read position pointer
     * @param out
     * @param length
     */
    virtual size_t get(uint8_t *out, size_t length) = 0;

    /**
     * Move read position pointer in buffer.
     * @param bytes
     */
    virtual void moveReadPointerBy(size_t bytes) = 0;

    /**
     * Write data to buffer. Usually this method used for testing purposes.
     * @param in input data.
     * @param length length of input data
     * @return
     */
    virtual size_t write(uint8_t *buffer, size_t length) = 0;

    virtual uint8_t *getBuffer() = 0;

    virtual void clear() = 0;
};
#endif
