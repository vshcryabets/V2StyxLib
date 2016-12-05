#ifndef ICHANNEL_DRIVER_H_
#define ICHANNEL_DRIVER_H_

#include <vector>
#include "ILogListener.h"
#include "server/IMessageTransmitter.h"
#include "handlers/IMessageProcessor.h"

class IChannelDriver : public IMessageTransmitter {
public:
    StyxThread start(int iounit);
    void setTMessageHandler(IMessageProcessor* handler);
    void setRMessageHandler(IMessageProcessor* handler);
    void setLogListener(ILogListener* listener);

    /**
     * Get all active clients.
     * @return all active clients.
     */
    std::vector<ClientDetails> getClients();

    bool isConnected();
    bool isStarted();

    IMessageProcessor* getTMessageHandler();
    IMessageProcessor* getRMessageHandler();
};

#endif // ICHANNEL_DRIVER_H_
