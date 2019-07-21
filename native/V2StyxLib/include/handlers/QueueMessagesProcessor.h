/*
 * RMessagesProcessor.h
 *
 */

#ifndef QUEUE_MESSAGESPROCESSOR_H_
#define QUEUE_MESSAGESPROCESSOR_H_

#include <queue>
#include <pthread.h>
#include "types.h"
#include "messages/base/StyxMessage.h"
#include "handlers/IMessageProcessor.h"

class QueueMessageProcessorPair {
public:
    StyxMessage *mMessage;
    ClientDetails *mTransmitter;
};

class QueueMessagesProcessor : public IMessageProcessor, IRunnable {
protected:
    std::queue<QueueMessageProcessorPair> mQueue;
    Condition mCondition;
    StyxThread mThread;
    size_t mHandledPackets;
    size_t mErrorPackets;
    StyxString mTag;
public:
    QueueMessagesProcessor(StyxString tag);
	virtual ~QueueMessagesProcessor();

	virtual void addClient(ClientDetails *state);
	virtual void removeClient(ClientDetails *state);
	virtual void postPacket(StyxMessage *message, ClientDetails *target);
	virtual void close();
    virtual void* run();
    virtual size_t getReceivedPacketsCount();
    virtual size_t getReceivedErrorPacketsCount();
};

#endif /* QUEUE_MESSAGESPROCESSOR_H_ */
