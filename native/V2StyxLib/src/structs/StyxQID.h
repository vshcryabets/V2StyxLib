/*
 * StyxQID.h
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */
#include "../io/IStyxDataReader.h"
#include "../io/IStyxDataWriter.h"

#ifndef STYXQID_H_
#define STYXQID_H_
#include "../types.h"

class StyxQID {
private:
	QIDType mType; //the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word.
	uint32_t mVersion; // version number for given path
	uint64_t mPath; //the file server's unique identification for the file
public:
	static const size_t CONTENT_SIZE = 13;
	StyxQID(IStyxDataReader *input);
	StyxQID(QIDTypeEnum type, uint32_t version, uint64_t path);
	virtual ~StyxQID();
	void writeBinaryTo(IStyxDataWriter *output);
};

#endif /* STYXQID_H_ */
