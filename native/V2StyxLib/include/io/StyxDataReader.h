/*
 * StyxDataReader.h
 *
 *  Created on: May 29, 2012
 *      Author: mrco
 */

#ifndef STYXDATAREADER_H_
#define STYXDATAREADER_H_
#include "io/IStyxDataReader.h"
#include "io/IStyxBuffer.h"
#include "exceptions/StyxException.h"

class StyxDataReader : public IStyxDataReader {
private:
protected:
	static const size_t sDataBufferSize = 16;
	uint8_t* mInternalBuffer;
	IStyxBuffer *mBuffer;
	uint64_t readInteger(size_t bytes);
	uint64_t getInteger(size_t bytes);
public:
	StyxDataReader(IStyxBuffer *buffer);
	~StyxDataReader();
	size_t getInternalBufferSize();
	// ================================================================
	// IStyxDataReader methods
	// ================================================================
	virtual std::string readUTFString();
	virtual uint8_t readUInt8();
	virtual uint16_t readUInt16();
	virtual uint32_t readUInt32();
	virtual uint64_t readUInt64();
	virtual uint32_t getUInt32();
	virtual size_t read(uint8_t *data, size_t dataLength);
};

#endif /* STYXDATAREADER_H_ */
