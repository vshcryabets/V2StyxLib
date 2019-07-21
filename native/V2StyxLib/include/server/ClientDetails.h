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
#include "exceptions/StyxErrorMessageException.h"
#include "io/StyxDataWriter.h"
#include "io/StyxDataReader.h"
#include "utils/FIDPoll.h"

class ClientDetails {
protected:
	std::map<StyxFID,IVirtualStyxFile*> mAssignedFiles;
	IChannelDriver *mDriver;
	uint32_t mClientId;
	std::map<StyxTAG, StyxTMessage*> mMessagesMap;
    MessageTagPoll mTags;
    FIDPoll mFids;
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
    IVirtualStyxFile* getAssignedFile(StyxFID fid) throw(StyxErrorMessageException);
    void unregisterClosedFile(StyxFID fid);
	void registerOpenedFile(StyxFID fid, IVirtualStyxFile *file);
	IChannelDriver* getDriver();
	uint32_t getId();
	virtual StyxString toString();
	FIDPoll* getFIDPoll();
    StyxTMessage* findTMessageAndRelease(StyxTAG tag); 
    void releaseFID(StyxTMessageFID* message);
	MessageTagPoll* getTagPoll();
    void putTMessage(StyxTAG tag, StyxTMessage* message);
};

#endif /* CLIENTSTATE_H_ */
