/*
 * IVirtualStyxFile.h
 *  Virtual styx file interface
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */
#ifndef IVirtualStyxFile_H_
#define IVirtualStyxFile_H_
#include <string>
#include "../classes.h"
#include "../types.h"
using namespace std;

class IStyxDataReader
{
public:
	virtual ~IStyxDataReader() {};
    uint8_t readUInt8() = 0;
    uint16_t readUInt16() = 0;
    uint32_t readUInt32() = 0;
    uint64_t readUInt64() = 0;
    uint32_t getUInt32()  = 0;
    std::string readUTFString();
    uint32_t read(uint8_t *data, size_t offset, size_t dataLength);
};
#endif
