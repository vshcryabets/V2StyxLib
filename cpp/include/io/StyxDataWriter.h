/*
 * StyxDataWriter.h
 *
 *  Created on: May 31, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef STYXDATAWRITER_H_
#define STYXDATAWRITER_H_
#include "IStyxDataWriter.h"
#include <string>

class StyxDataWriter : public IStyxDataWriter {
private:
public:
	StyxDataWriter();
	~StyxDataWriter();
	// ==================================================
	// IStyxDataWriter methods
	// ==================================================
	virtual void writeUInt8(uint8_t val);
	virtual void writeUInt16(uint16_t val);
	virtual void writeUInt32(uint32_t val);
	virtual void writeUInt64(uint64_t value);
	virtual void writeUTFString(StyxString *string);
};

#endif /* STYXDATAWRITER_H_ */
