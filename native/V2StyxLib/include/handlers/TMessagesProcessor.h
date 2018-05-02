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

class TMessagesProcessor : public QueueMessagesProcessor {
protected:
	ConnectionDetails mConnectionDetails;
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
