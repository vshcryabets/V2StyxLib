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
#include "../vfs/IVirtualStyxDirectory.h"
#include "../structs/StyxQID.h"
#include <vector>
#include "../structs/StyxStat.h"
#include <map>
#include "../io/StyxByteBufferWritable.h"
using namespace std;

typedef map<ClientState*, StyxByteBufferWritable*> ClientsMap;

class MemoryStyxDirectory : public IVirtualStyxDirectory
{
private:
	StyxString mName;
	StyxString *mOwner;
	StyxQID *mQID;
	StyxStat *mStat;
	FileList mFiles;
	ClientsMap mBuffersMap;
public:
	MemoryStyxDirectory(std::string name);
	virtual ~MemoryStyxDirectory();
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
	StyxString* getName();
	Date getAccessTime();
	Date getModificationTime();
	uint64_t getLength();
	StyxString* getOwnerName();
	StyxString* getGroupName();
	StyxString* getModificationUser();
	/**
	 * Open file
	 * @param mode
	 * @throws IOException
	 */
	bool open(ClientState *client, int mode);
	/**
	 * Close file
	 * @param mode
	 */
	void close(ClientState *client);
	/**
	 * Read from file
	 * @param offset offset from begining of the file
	 * @param count number of bytes to read
	 * @return number of bytes that was readed into the buffer
	 */
	size_t read(ClientState *client, uint8_t* buffer, uint64_t offset, size_t count);
	virtual IVirtualStyxFile* walk(std::vector<StyxString*> *pathElements, std::vector<StyxQID*> *qids);
	/**
	 * Write data to file
	 * @param client
	 * @param data
	 * @param offset
	 * @return
	 * @throws StyxErrorMessageException
	 */
	int write(ClientState *client, uint8_t* data, uint64_t offset);
	// ================================================================
	// IVirualStyxDirectory
	// ================================================================
	IVirtualStyxFile* getFile(StyxString *path);
	IVirtualStyxDirectory* getDirectory(StyxString *path);
	void onConnectionClosed(ClientState *state);
};

#endif /* MEMORYSTYXDIRECTORY_H_ */
