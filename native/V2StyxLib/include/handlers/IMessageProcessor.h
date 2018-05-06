/*
 * IMessageProcessor.h
 *
 *  Created on: Oct 6, 2016
 *      Author: vova
 */

#ifndef INCLUDE_HANDLERS_IMESSAGEPROCESSOR_H_
#define INCLUDE_HANDLERS_IMESSAGEPROCESSOR_H_

#include "exceptions/StyxException.h"
#include "server/ClientDetails.h"

class IMessageProcessor {
public:
	virtual ~IMessageProcessor() {};
    virtual void addClient(ClientDetails *state) = 0;
    virtual void removeClient(ClientDetails *state) = 0;
    virtual void postPacket(StyxMessage *message, ClientDetails *target) = 0;
    virtual void processPacket(StyxMessage *message, ClientDetails *target) throw(StyxException) = 0;
    virtual size_t getReceivedPacketsCount() = 0;
    virtual size_t getReceivedErrorPacketsCount() = 0;
};

#endif /* INCLUDE_HANDLERS_IMESSAGEPROCESSOR_H_ */