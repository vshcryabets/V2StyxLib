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
public:
	RMessagesProcessor(StyxString tag);
	virtual ~RMessagesProcessor();
	virtual void processPacket(StyxMessage *message, ClientDetails *client) throw(StyxException);
};

#endif /* RMESSAGESPROCESSOR_H_ */
