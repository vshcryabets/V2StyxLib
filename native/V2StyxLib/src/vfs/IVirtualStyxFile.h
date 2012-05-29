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
	/**
	 * @return unic ID of the file
	 */
	virtual StyxQID* getQID() = 0;

	virtual StyxStat* getStat() = 0;
	/**
	 * @return file access mode
	 */
	virtual int getMode() = 0;
	/**
	 * @return file name
	 */
	virtual string getName() = 0;
	virtual Date getAccessTime() = 0;
	virtual Date getModificationTime() = 0;
	virtual int128_t getLength() = 0;
	virtual string getOwnerName() = 0;
	virtual string getGroupName() = 0;
	virtual string getModificationUser() = 0;
	/**
	 * Open file
	 * @param mode
	 * @throws IOException
	 */
	virtual bool open(ClientState *client, int mode) = 0;
	/**
	 * Close file
	 * @param mode
	 */
	virtual void close(ClientState *client) = 0;
	/**
	 * Read from file
	 * @param offset offset from begining of the file
	 * @param count number of bytes to read
	 * @return number of bytes that was read into the buffer
	 */
	virtual long read(ClientState *client, int8_t* buffer, int128_t offset, long count) = 0;
//	virtual IVirtualStyxFile walk(List<String> pathElements, List<StyxQID> qids) = 0;
	/**
	 * Write data to file
	 * @param client
	 * @param data
	 * @param offset
	 * @return
	 * @throws StyxErrorMessageException
	 */
	virtual int write(ClientState *client, int8_t* data, int128_t offset) = 0;
};
#endif
