/*
 * StyxBufferOperations.h
 *
 *  Created on: May 22, 2012
 *      Author: mrco
 */

#ifndef STYXBUFFEROPERATIONS_H_
#define STYXBUFFEROPERATIONS_H_
#include <string>
#include "types.h"

class StyxBufferOperations {
protected:
	static const int sDataBufferSize = 16;
private:
	int8_t  *mDataBuffer;
public:
	StyxBufferOperations();
	virtual ~StyxBufferOperations();
	virtual uint32_t getInteger(int bytes) = 0;
	virtual void clear();
	virtual void limit(size_t value);
	virtual void write(int8_t* data);
	virtual void write(int8_t* data, size_t offset, size_t count);
	virtual int read(int8_t* mData, size_t offset, size_t count);
	uint32_t readUInt32();
	uint16_t readUInt16();
	uint8_t readUInt8();
	uint64_t readUInt64();
	uint32_t getUInt32();
	void writeUInt32(uint32_t val);
	void writeUShort(uint16_t val);
	void writeUByte(uint8_t val);
	void writeUInt64(uint64_t value);

	void writeUTF(std::string *string);

	void writeInteger(int bytes, uint64_t value);
	uint64_t readInteger(int bytes);
	std::string readUTF();
};

#endif /* STYXBUFFEROPERATIONS_H_ */
