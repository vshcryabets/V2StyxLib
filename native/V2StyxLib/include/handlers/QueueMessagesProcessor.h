/*
 * RMessagesProcessor.h
 *
 */

#ifndef QUEUE_MESSAGESPROCESSOR_H_
#define QUEUE_MESSAGESPROCESSOR_H_

#include <vector>
#include "types.h"
#include "messages/base/StyxMessage.h"
#include "handlers/IMessageProcessor.h"

class QueueMessageProcessorPair {
public:
    StyxMessage *mMessage;
    ClientDetails *mTransmitter;
};

class QueueMessagesProcessor : public IMessageProcessor {
protected:
    std::vector<QueueMessageProcessorPair> mQueue;
public:
    QueueMessagesProcessor();
	virtual ~QueueMessagesProcessor();

	virtual void postPacket(StyxMessage *message, ClientDetails *target);
	virtual void close();
};

#endif /* QUEUE_MESSAGESPROCESSOR_H_ */
