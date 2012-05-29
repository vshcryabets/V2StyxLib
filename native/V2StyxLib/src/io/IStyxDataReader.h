/*
 * IVirtualStyxFile.h
 *  Virtual styx file interface
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */
#ifndef IStyxDataReader_H_
#define IStyxDataReader_H_
#include <string>
#include "../classes.h"
#include "../types.h"
using namespace std;

class IStyxDataReader
{
public:
	virtual ~IStyxDataReader() {};
    virtual uint8_t readUInt8() = 0;
    virtual uint16_t readUInt16() = 0;
    virtual uint32_t readUInt32() = 0;
    virtual uint64_t readUInt64() = 0;
    virtual uint32_t getUInt32()  = 0;
    virtual std::string readUTFString();
    virtual size_t read(uint8_t *data, size_t offset, size_t dataLength);
};
#endif
