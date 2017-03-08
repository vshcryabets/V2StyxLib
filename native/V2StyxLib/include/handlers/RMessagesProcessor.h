/*
 * RMessagesProcessor.h
 *
 */

#ifndef RMESSAGESPROCESSOR_H_
#define RMESSAGESPROCESSOR_H_

#include "handlers/QueueMessagesProcessor.h"
#include "types.h"

class RMessagesProcessor : public QueueMessagesProcessor {
protected:
    size_t mReceivedCount;
    size_t mErrorCount;
    std::string mTag;
public:
	RMessagesProcessor(ConnectionDetails details, IVirtualStyxFile *root);
	virtual ~RMessagesProcessor();

	virtual void addClient(ClientDetails *state);
	virtual void removeClient(ClientDetails *state);
	virtual void processPacket(StyxMessage *message, ClientDetails *client) throw();
	virtual size_t getReceivedPacketsCount();
	virtual size_t getReceivedErrorPacketsCount();
};

#endif /* RMESSAGESPROCESSOR_H_ */
