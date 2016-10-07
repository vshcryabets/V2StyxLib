/*
 * StdOutFile.h
 *
 *  Created on: Jun 5, 2012
 *      Author: mrco
 */

#ifndef STDOUTFILE_H_
#define STDOUTFILE_H_

#include "vfs/MemoryStyxFile.h"

class StdOutFile: public MemoryStyxFile {
public:
	StdOutFile(std::string name);
	~StdOutFile();
	/**
	 * Write data to file
	 * @param client
	 * @param data
	 * @param offset
	 * @return
	 */
	virtual size_t write(ClientDetails *client, uint8_t* data, uint64_t offset, size_t count);
};

#endif /* STDOUTFILE_H_ */
