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
#include <vector>
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
	virtual StyxString getName() = 0;
	virtual Date getAccessTime() = 0;
	virtual Date getModificationTime() = 0;
	virtual uint64_t getLength() = 0;
	virtual StyxString getOwnerName() = 0;
	virtual StyxString getGroupName() = 0;
	virtual StyxString getModificationUser() = 0;
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
	virtual size_t read(ClientState *client, uint8_t* buffer, uint64_t offset, size_t count) = 0;
	virtual IVirtualStyxFile* walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID*> *qids) = 0;
	/**
	 * Write data to file
	 * @param client
	 * @param data
	 * @param offset
	 * @return
	 */
	virtual size_t write(ClientState *client, uint8_t* data, uint64_t offset) = 0;
    /**
     * Will be called when client close connection to this server
     * @param state
     */
   virtual void onConnectionClosed(ClientState *state) = 0;
};
#endif
