/*
 * StyxDataWriter.cpp
 *
 *  Created on: May 31, 2012
 *      Author: mrco
 */

#include "StyxDataWriter.h"

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
	uint temp[] = { val&0xFF, (val>>8)&0xFF)};
	write(temp, 0, 2);
}
void StyxDataWriter::writeUInt32(uint32_t val) {
		uint temp[] = { val&0xFF, (val>>8)&0xFF),
		(val>>16)&0xFF),  (val>>24)&0xFF };
	write(temp, 0, 4);
}
void StyxDataWriter::writeUInt64(uint64_t value) {
	write(value.getBytes());
}
void StyxDataWriter::writeUTFString(String string) {
	byte [] data = string.getBytes(sUTFCharset);
	writeUInt16(data.length);
	write(data);
}
