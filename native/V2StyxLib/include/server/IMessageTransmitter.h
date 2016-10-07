#ifndef IMESSAGE_TRANSMITTER_H_
#define IMESSAGE_TRANSMITTER_H_

class IMessageTransmitter {
public:
    bool sendMessage(StyxMessage answer, ClientDetails recepient) throw();
    size_t getTransmittedCount();
    size_t getErrorsCount();
};

#endif // IMESSAGE_TRANSMITTER_H_
