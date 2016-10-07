/*
 * StdOutFile.cpp
 *
 *  Created on: Jun 5, 2012
 *      Author: vshcryabets@gmail.com
 */

#include "StdOutFile.h"
#ifdef WIN32
#include "io.h"
#define STDIN_FILENO 0
#define STDOUT_FILENO 1
#define STDERR_FILENO 2
#else
#include <unistd.h>
#endif

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
size_t StdOutFile::write(ClientDetails *client, uint8_t* data, uint64_t offset, size_t count) {
	::write(STDOUT_FILENO, data, count);
	return  count;
}
