/*
 * MemoryStyxDirectory.h
 *
 *  Created on: May 20, 2012
 *      Author: mrco
 */

#ifndef MEMORYSTYXDIRECTORY_H_
#define MEMORYSTYXDIRECTORY_H_
#include <string>
#include "IVirtualStyxDirectory.h"

class MemoryStyxDirectory : public IVirtualStyxDirectory
{
private:
	std::string mName;
public:
	MemoryStyxDirectory(std::string name);
	~MemoryStyxDirectory();
	// ================================================================
	// IVirualStyxFile
	// ================================================================
	/**
	 * @return unic ID of the file
	 */
	StyxQID* getQID();

	StyxStat* getStat();
	/**
	 * @return file access mode
	 */
	int getMode();
	/**
	 * @return file name
	 */
	string getName();
	Date getAccessTime();
	Date getModificationTime();
	int128_t getLength();
	string getOwnerName();
	string getGroupName();
	string getModificationUser();
	/**
	 * Open file
	 * @param mode
	 * @throws IOException
	 */
	bool open(ClientState client, int mode);
	/**
	 * Close file
	 * @param mode
	 */
	void close(ClientState client);
	/**
	 * Read from file
	 * @param offset offset from begining of the file
	 * @param count number of bytes to read
	 * @return number of bytes that was readed into the buffer
	 */
	long read(ClientState client, int8_t* buffer, int128_t offset, long count);
	//	IVirtualStyxFile walk(List<String> pathElements, List<StyxQID> qids) = 0;
	/**
	 * Write data to file
	 * @param client
	 * @param data
	 * @param offset
	 * @return
	 * @throws StyxErrorMessageException
	 */
	int write(ClientState client, int8_t* data, int128_t offset);
	// ================================================================
	// IVirualStyxDirectory
	// ================================================================
	IVirtualStyxFile* getFile(string path);
	IVirtualStyxDirectory* getDirectory(string path);
};

#endif /* MEMORYSTYXDIRECTORY_H_ */
