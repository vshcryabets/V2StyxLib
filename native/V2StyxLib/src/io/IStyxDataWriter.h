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

class IVirtualStyxFile
{
public:
	virtual ~IVirtualStyxFile() {};
    void writeUInt8(uint8_t val) = 0;
    void writeUInt16(uint16_t val) = 0;
    void writeUInt32(uint32_t val) = 0;
    void writeUInt64(uint64_t value) = 0;
    void writeUTFString(std::string string) = 0;
    int write(uint8_t[] data, size_t offset, size_t count) = 0;
    void clear() = 0;
    void limit(size_t limit) = 0;
};
#endif
