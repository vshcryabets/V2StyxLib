/*
 * MemoryStyxDirectory.h
 *
 *  Created on: May 20, 2012
 *      Author: vshcryabets@gmail.com
 */

#ifndef MEMORYSTYXDIRECTORY_H_
#define MEMORYSTYXDIRECTORY_H_
#include <string>
#include "../structs/StyxQID.h"
#include "../structs/StyxStat.h"
#include "../types.h"
#include "../structs/StyxQID.h"
#include <vector>
#include "../structs/StyxStat.h"
#include <map>
#include "../io/StyxByteBufferWritable.h"
#include "MemoryStyxFile.h"
using namespace std;

typedef map<ClientDetails*, StyxByteBufferWritable*> ClientsMap;
typedef vector<IVirtualStyxFile*> FileList;

class MemoryStyxDirectory : public MemoryStyxFile
{
private:
	FileList mFiles;
	ClientsMap mBuffersMap;
public:
	MemoryStyxDirectory(std::string name);
	virtual ~MemoryStyxDirectory();
    /**
     * Add child file to this directory
     * @param file child file
     */
    void addFile(IVirtualStyxFile *file);
	// ================================================================
	// IVirualStyxFile
	// ================================================================
	/**
	 * @return file access mode
	 */
	int getMode();
	/**
	 * Open file
	 * @param mode
	 */
	bool open(ClientDetails *client, int mode);
	/**
	 * Close file
	 * @param mode
	 */
	void close(ClientDetails *client);
	/**
	 * Read from file
	 * @param offset offset from begining of the file
	 * @param count number of bytes to read
	 * @return number of bytes that was readed into the buffer
	 */
	size_t read(ClientDetails *client, uint8_t* buffer, uint64_t offset, size_t count);
	virtual IVirtualStyxFile* walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID*> *qids);
	/**
	 * Write data to file
	 * @param client
	 * @param data
	 * @param offset
	 */
	void onConnectionClosed(ClientDetails *state);
};

#endif /* MEMORYSTYXDIRECTORY_H_ */
