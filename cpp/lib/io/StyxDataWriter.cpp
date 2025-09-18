/*
 * StyxDataWriter.cpp
 *
 *  Created on: May 31, 2012
 *      Author: vschryabets@gmail.com
 */

#include "io/StyxDataWriter.h"

StyxDataWriter::StyxDataWriter() {
	// TODO Auto-generated constructor stub

}

StyxDataWriter::~StyxDataWriter() {
	// TODO Auto-generated destructor stub
}

void StyxDataWriter::writeUInt8(uint8_t val) {
	write(&val, 0, 1);
}
void StyxDataWriter::writeUInt16(uint16_t val) {
	uint8_t temp[] = { val&0xFF, (val>>8)&0xFF};
	write(temp, 0, 2);
}
void StyxDataWriter::writeUInt32(uint32_t val) {
	uint8_t temp[] = { val&0xFF, (val>>8)&0xFF, (val>>16)&0xFF,  (val>>24)&0xFF };
	write(temp, 0, 4);
}
void StyxDataWriter::writeUInt64(uint64_t val) {
	uint8_t temp[] = { val&0xFF, (val>>8)&0xFF, (val>>16)&0xFF,  (val>>24)&0xFF,
			(val>>32)&0xFF, (val>>40)&0xFF, (val>>48)&0xFF,  (val>>56)&0xFF};
	write(temp, 0, 8);
}
void StyxDataWriter::writeUTFString(std::string *string) {
	writeUInt16(string->length());
	write((const uint8_t*)string->c_str(), 0, string->length());
}
