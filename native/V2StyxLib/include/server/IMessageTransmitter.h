#ifndef IMESSAGE_TRANSMITTER_H_
#define IMESSAGE_TRANSMITTER_H_

class IMessageTransmitter {
public:
	virtual ~IMessageTransmitter() {};
    virtual bool sendMessage(StyxMessage *answer, ClientDetails *recepient) throw(StyxException) = 0;
    virtual size_t getTransmittedCount() = 0;
    virtual size_t getErrorsCount() = 0;
    virtual void close() throw(StyxException) = 0;
    virtual StyxString toString() = 0;
};

#endif // IMESSAGE_TRANSMITTER_H_
