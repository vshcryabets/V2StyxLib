/*
 * ClientState.h
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef CLIENTSTATE_H_
#define CLIENTSTATE_H_
#include "types.h"
#include "classes.h"
#include <map>
#include "messages/base/StyxMessage.h"
#include "io/StyxByteBufferReadable.h"
#include "messages/StyxTCreateMessage.h"
#include "vfs/IVirtualStyxFile.h"
#include "server/IChannelDriver.h"
#include "utils/Polls.h"
#include "exceptions/StyxErrorMessageException.h"
#include "io/StyxDataWriter.h"
#include "io/StyxDataReader.h"

class ClientDetails {
protected:
	std::map<StyxFID,IVirtualStyxFile*> mAssignedFiles;
	IChannelDriver *mDriver;
	uint32_t mClientId;
	Polls* mPolls; // TODO probably we can move polls here, and remove Polls class
	Credentials mCredentials;
	StyxBuffer mOutputBuffer;
	StyxDataWriter mOutputWriter;
	StyxByteBufferReadable mInputBuffer;
    StyxDataReader mInputReader;
public:
	ClientDetails(IChannelDriver* driver, uint32_t iounit, uint32_t id);
	~ClientDetails();
	void setCredentials(Credentials credentials);
	Credentials getCredentials();
	StyxDataWriter* getOutputWritter();
	StyxBuffer* getOutputBuffer();
	StyxByteBufferReadable* getInputBuffer();
	IStyxDataReader* getInputReader();
    /**
     * Get polls assigned to this client.
     * @return polls assigned to this client.
     */
    Polls *getPolls();

    IVirtualStyxFile* getAssignedFile(StyxFID fid) throw(StyxErrorMessageException);

    void unregisterClosedFile(StyxFID fid);
	void registerOpenedFile(StyxFID fid, IVirtualStyxFile *file);

	IChannelDriver* getDriver();
	uint32_t getId();
	virtual StyxString toString();
};

#endif /* CLIENTSTATE_H_ */
