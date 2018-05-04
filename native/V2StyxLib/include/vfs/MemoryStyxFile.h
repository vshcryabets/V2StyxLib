/*
 * MemoryStyxFile.h
 *
 *  Created on: Jun 3, 2012
 *      Author: mrco
 */

#ifndef MEMORYSTYXFILE_H_
#define MEMORYSTYXFILE_H_

#include "IVirtualStyxFile.h"
#include "./messages/base/structs/StyxQID.h"
#include "./messages/base/structs/StyxStat.h"
#include <string>

class MemoryStyxFile: public IVirtualStyxFile {
protected:
	StyxString mName;
	StyxString mOwner;
	StyxQID mQID;
	StyxStat *mStat;
public:
	MemoryStyxFile(std::string name);
	virtual ~MemoryStyxFile();
	// =======================================================
	// Virtual methods
	// =======================================================
	/**
	 * @return unic ID of the file
	 */
	virtual StyxQID getQID();

	virtual StyxStat* getStat();
	/**
	 * @return file access mode
	 */
	virtual int getMode();
	/**
	 * @return file name
	 */
	virtual StyxString getName();
	virtual Date getAccessTime();
	virtual Date getModificationTime();
	virtual uint64_t getLength();
	virtual StyxString getOwnerName();
	virtual StyxString getGroupName();
	virtual StyxString getModificationUser();
	/**
	 * Open file
	 * @param mode
	 * @throws IOException
	 */
	virtual bool open(ClientDetails *client, int mode);
	/**
	 * Close file
	 * @param mode
	 */
	virtual void close(ClientDetails *client);
	/**
	 * Read from file
	 * @param offset offset from begining of the file
	 * @param count number of bytes to read
	 * @return number of bytes that was read into the buffer
	 */
	virtual size_t read(ClientDetails *client, uint8_t* buffer, uint64_t offset, size_t count);
	virtual IVirtualStyxFile* walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID> *qids);
	/**
	 * Write data to file
	 * @param client
	 * @param data
	 * @param offset
	 * @return
	 */
	virtual size_t write(ClientDetails *client, uint8_t* data, uint64_t offset, size_t count);
	/**
	 * Will be called when client close connection to this server
	 * @param state
	 */
	virtual void onConnectionClosed(ClientDetails *state);
};

#endif /* MEMORYSTYXFILE_H_ */
