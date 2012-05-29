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
	uint64_t readInteger(size_t bytes);
public:
	StyxDataReader();
	~StyxDataReader();
	// ================================================================
	// IStyxDataReader methods
	// ================================================================
	virtual std::string readUTFString();
	virtual uint8_t readUInt8();
	virtual uint16_t readUInt16();
	virtual uint32_t readUInt32();
	virtual uint64_t readUInt64();
	virtual uint32_t getUInt32();
};

#endif /* STYXDATAREADER_H_ */
