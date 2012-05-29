/*
 * StyxDataReader.h
 *
 *  Created on: May 29, 2012
 *      Author: mrco
 */

#ifndef STYXDATAREADER_H_
#define STYXDATAREADER_H_
#include "IStyxDataReader.h"

class StyxDataReader : public IStyxDataReader {
private:
protected:
	size_t sDataBufferSize = 8;
	uint8_t *mDataBuffer;

	uint64_t readInteger(size_t bytes);
public:
	StyxDataReader();
	~StyxDataReader();
	// ================================================================
	// IStyxDataReader methods
	// ================================================================
	std::string readUTFString();
	uint8_t readUInt8();
	uint16_t readUInt16();
	uint32_t readUInt32();
	uint64_t readUInt64();
	//abstract int read(byte[] data, int offset, int count);
	uint32_t getUInt32();
};

#endif /* STYXDATAREADER_H_ */
