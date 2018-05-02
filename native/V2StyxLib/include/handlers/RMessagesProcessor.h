/*
 * RMessagesProcessor.h
 *
 */

#ifndef RMESSAGESPROCESSOR_H_
#define RMESSAGESPROCESSOR_H_

#include "handlers/QueueMessagesProcessor.h"
#include "types.h"

/**
 * Class that processes RMessages (i.e answer from server).
 */
class RMessagesProcessor : public QueueMessagesProcessor {
protected:
    size_t mReceivedCount;
    size_t mErrorCount;
    StyxString mTag;
public:
	RMessagesProcessor(StyxString tag);
	virtual ~RMessagesProcessor();

	virtual void addClient(ClientDetails *state);
	virtual void removeClient(ClientDetails *state);
	virtual void processPacket(StyxMessage *message, ClientDetails *client) throw(StyxException);
	virtual size_t getReceivedPacketsCount();
	virtual size_t getReceivedErrorPacketsCount();
};

#endif /* RMESSAGESPROCESSOR_H_ */
