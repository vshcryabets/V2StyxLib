/*
 * IVirtualStyxFile.h
 *  Virtual styx file interface
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */
#ifndef IStyxDataWriter_H_
#define IStyxDataWriter_H_
#include <string>
#include "../classes.h"
#include "../types.h"
using namespace std;

class IStyxDataWriter
{
public:
	virtual ~IStyxDataWriter() {};
	virtual void writeUInt8(uint8_t val) = 0;
	virtual void writeUInt16(uint16_t val) = 0;
	virtual void writeUInt32(uint32_t val) = 0;
	virtual void writeUInt64(uint64_t value) = 0;
	virtual void writeUTFString(std::string string) = 0;
	virtual size_t write(uint8_t* data, size_t offset, size_t count) = 0;
	virtual void clear() = 0;
	virtual void limit(size_t limit) = 0;
};
#endif
