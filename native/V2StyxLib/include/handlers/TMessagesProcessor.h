/*
 * TMessagesProcessor.h
 *
 *  Created on: Dec 5, 2016
 *      Author: vova
 */

#ifndef INCLUDE_HANDLERS_TMESSAGESPROCESSOR_H_
#define INCLUDE_HANDLERS_TMESSAGESPROCESSOR_H_

#include "types.h"
#include "vfs/IVirtualStyxFile.h"
#include "handlers/QueueMessagesProcessor.h"
#include "messages/StyxTAttachMessage.h"
#include "messages/StyxTAuthMessage.h"
#include "messages/StyxTWalkMessage.h"
#include "messages/StyxTOpenMessage.h"
#include "messages/StyxTReadMessage.h"
#include "messages/StyxTWriteMessage.h"
#include "messages/StyxTWStatMessage.h"
#include "messages/StyxTCreateMessage.h"

class TMessagesProcessor : public QueueMessagesProcessor {
protected:
	ConnectionDetails mConnectionDetails;
	IVirtualStyxFile *mRoot;
	size_t mHandledPackets;
	size_t mErrorPackets;
	size_t mAnswerPackets;
private:
	static const size_t DEFAULT_PACKET_HEADER_SIZE = 24;
	StyxMessage* processAttach(ClientDetails* clientDetails, StyxTAttachMessage* msg);
	StyxMessage* processAuth(ClientDetails* clientDetails, StyxTAuthMessage* msg);
	StyxMessage* processClunk(ClientDetails* clientDetails, StyxTMessageFID* msg) throw(StyxErrorMessageException);
	StyxMessage* processWalk(ClientDetails* clientDetails, StyxTWalkMessage* msg) throw(StyxErrorMessageException);
	StyxMessage* processOpen(ClientDetails* clientDetails, StyxTOpenMessage* msg) throw(StyxErrorMessageException);
	StyxMessage* processRead(ClientDetails* clientDetails, StyxTReadMessage* msg) throw(StyxErrorMessageException);
	StyxMessage* processWrite(ClientDetails* clientDetails, StyxTWriteMessage* msg) throw(StyxErrorMessageException);
	StyxMessage* processWStat(ClientDetails* clientDetails, StyxTWStatMessage* msg) throw(StyxErrorMessageException);
	StyxMessage* processCreate(ClientDetails* clientDetails, StyxTCreateMessage* msg) throw(StyxErrorMessageException);
	StyxMessage* processRemove(ClientDetails* clientDetails, StyxTMessageFID* msg) throw(StyxErrorMessageException);
public:
	TMessagesProcessor(ConnectionDetails details, IVirtualStyxFile *root);
	virtual ~TMessagesProcessor();
	virtual void addClient(ClientDetails *state);
	virtual void removeClient(ClientDetails *state);
	virtual void processPacket(StyxMessage *message, ClientDetails *target) throw(StyxException);
	virtual size_t getReceivedPacketsCount();
    virtual size_t getReceivedErrorPacketsCount();
};

#endif /* INCLUDE_HANDLERS_TMESSAGESPROCESSOR_H_ */
