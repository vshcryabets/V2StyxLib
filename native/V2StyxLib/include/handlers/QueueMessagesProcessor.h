/*
 * RMessagesProcessor.h
 *
 */

#ifndef QUEUE_MESSAGESPROCESSOR_H_
#define QUEUE_MESSAGESPROCESSOR_H_

#include <queue>
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
    StyxThread mThread;
public:
    QueueMessagesProcessor();
	virtual ~QueueMessagesProcessor();

	virtual void postPacket(StyxMessage *message, ClientDetails *target);
	virtual void close();
    virtual void* run();
};

#endif /* QUEUE_MESSAGESPROCESSOR_H_ */
