#ifndef ICHANNEL_DRIVER_H_
#define ICHANNEL_DRIVER_H_

#include <vector>
#include "ILogListener.h"
#include "server/IMessageTransmitter.h"
#include "handlers/IMessageProcessor.h"

class IChannelDriver : public IMessageTransmitter {
public:
	virtual ~IChannelDriver() {};
    virtual StyxThread start(int iounit) = 0;
    virtual void setTMessageHandler(IMessageProcessor* handler) = 0;
    virtual void setRMessageHandler(IMessageProcessor* handler) = 0;
    virtual void setLogListener(ILogListener* listener) = 0;

    /**
     * Get all active clients.
     * @return all active clients.
     */
    virtual std::vector<ClientDetails> getClients() = 0;

    virtual bool isConnected() = 0;
    virtual bool isStarted() = 0;

    virtual IMessageProcessor* getTMessageHandler() = 0;
    virtual IMessageProcessor* getRMessageHandler() = 0;
};

#endif // ICHANNEL_DRIVER_H_
