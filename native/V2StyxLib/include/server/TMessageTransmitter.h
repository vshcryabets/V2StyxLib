/*
 * TMessagesTRansmitter.h
 *
 */

#ifndef TMESSAGE_TRANSMITTER_H_
#define TMESSAGE_TRANSMITTER_H_

#include "types.h"
#include "server/IMessageTransmitter.h"
#include "server/ClientDetails.h"
#include "utils/SyncObject.h"

#warning why do we need this class? We can directly use ChannelDriver.
class TMessageTransmitter : public IMessageTransmitter {
public:
    class Listener {
    public:
        virtual void onChannelDisconnected(TMessageTransmitter *caller) = 0;
        virtual void onTrashReceived(TMessageTransmitter *caller) = 0;
    };

protected:
    size_t mTransmittedCount;
    size_t mErrorCount;
    Listener *mListener;

public:
    TMessageTransmitter();
	virtual ~TMessageTransmitter();
    void setListener(Listener* listener);
	virtual bool sendMessage(StyxMessage *message, ClientDetails *recepient) throw(StyxException);
    virtual StyxMessage* sendMessageAndWaitAnswer(StyxMessage *message, ClientDetails *recepient, 
        SyncObject* syncObject) throw(StyxException);
	virtual size_t getTransmittedCount();
	virtual size_t getErrorsCount();
	virtual void close() throw(StyxException);
	virtual StyxString toString();
};

#endif /* TMESSAGE_TRANSMITTER_H_ */
