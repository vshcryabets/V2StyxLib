/*
 * TMessagesTRansmitter.h
 *
 */

#ifndef TMESSAGE_TRANSMITTER_H_
#define TMESSAGE_TRANSMITTER_H_

#include "types.h"
#include "vfs/IVirtualStyxFile.h"

class TMessageTransmitter : public IMessageTransmitter {
public:
    class Listener {
    public:
        virtual void onSocketDisconnected(TMessageTransmitter *caller) = 0; // TODO why socket?
        virtual void onTrashReceived(TMessageTransmitter *caller) = 0;
    };

protected:
    size_t mTransmittedCount;
    size_t mErrorCount;
    Listener *mListener;

public:
    TMessageTransmitter(Listener *listener);
	virtual ~TMessageTransmitter();

	virtual bool sendMessage(StyxMessage *message, ClientDetails *recepient) throw(StyxException);
	virtual size_t getTransmittedCount();
	virtual size_t getErrorsCount();
	virtual void close() throw(StyxException);
	virtual StyxString toString();
};

#endif /* TMESSAGE_TRANSMITTER_H_ */
