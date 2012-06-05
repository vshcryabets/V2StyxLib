/*
 * StdOutFile.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#include "StdOutFile.h"

StdOutFile::StdOutFile(std::string name) :
	MemoryStyxFile(name) {
}

StdOutFile::~StdOutFile() {
}
/**
 * Write data to file
 * @param client
 * @param data
 * @param offset
 * @return
 */
size_t StdOutFile::write(ClientState *client, uint8_t* data, uint64_t offset, size_t count) {
	return  count;
			//::write(STDOUT_FILENO, data, count);
}
