/*
 * StyxQID.h
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */
#ifndef STYXQID_H_
#define STYXQID_H_

#include "./messages/base/enums/QIDType.h"
#include "./io/IStyxDataReader.h"
#include "./io/IStyxDataWriter.h"

class StyxQID {
private:
	QIDTypeEnum mType; //the type of the file (directory, etc.), represented as a bit vector corresponding to the high 8 bits of the file's mode word.
	uint32_t mVersion; // version number for given path
	uint64_t mPath; //the file server's unique identification for the file
public:
	static const size_t CONTENT_SIZE = 13;
	static const StyxQID EMPTY;
	StyxQID(IStyxDataReader *input);
	StyxQID(QIDTypeEnum type, uint32_t version, uint64_t path);
	virtual ~StyxQID();
	void writeBinaryTo(IStyxDataWriter *output);
	void setType(QIDTypeEnum type);
	QIDTypeEnum getType() const;
	uint32_t getVersion() const;
	uint64_t getPath() const;
	StyxString toString();
};

#endif /* STYXQID_H_ */
