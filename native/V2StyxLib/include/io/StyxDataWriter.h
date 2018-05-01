/*
 * StyxDataWriter.h
 *
 *  Created on: May 31, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXDATAWRITER_H_
#define STYXDATAWRITER_H_
#include "IStyxDataWriter.h"
#include <vector>

class StyxDataWriter : public IStyxDataWriter {
private:
    uint8_t mInternalBuffer[16];
    std::vector<uint8_t>* mBuffer;
	size_t mWritePosition;
	size_t mMaxWritePosition;
public:
	StyxDataWriter(std::vector<uint8_t>* buffer);
	~StyxDataWriter();
	// ==================================================
	// IStyxDataWriter methods
	// ==================================================
	virtual void writeUInt8(uint8_t val);
	virtual void writeUInt16(uint16_t val);
	virtual void writeUInt32(uint32_t val);
	virtual void writeUInt64(uint64_t value);
	virtual void writeUTFString(StyxString string);
	virtual size_t write(const uint8_t* data, size_t count);
	virtual void clear();
	virtual void limit(size_t limit);
	virtual size_t getPosition();
};

#endif /* STYXDATAWRITER_H_ */
