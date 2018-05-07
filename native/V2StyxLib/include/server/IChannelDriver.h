#ifndef ICHANNEL_DRIVER_H_
#define ICHANNEL_DRIVER_H_

#include <vector>

class IChannelDriver;
class IMessageProcessor;
class ILogListener;

#include "server/IMessageTransmitter.h"
#include "handlers/IMessageProcessor.h"
#include "utils/StyxThread.h"

class IChannelDriver : public IMessageTransmitter {
public:
	virtual ~IChannelDriver() {};
    virtual StyxThread* start(size_t iounit) throw(StyxException) = 0;
    virtual void setTMessageHandler(IMessageProcessor* handler) = 0;
    virtual void setRMessageHandler(IMessageProcessor* handler) = 0;
#ifdef USE_LOGGING
    virtual void setLogListener(ILogListener* listener) = 0;
#endif
    /**
     * Get all active clients.
     * @return all active clients.
     */
    virtual std::vector<ClientDetails*> getClients() = 0;

    virtual bool isConnected() = 0;
    virtual bool isStarted() = 0;

    virtual IMessageProcessor* getTMessageHandler() = 0;
    virtual IMessageProcessor* getRMessageHandler() = 0;
};

class ILogListener {
public:
	virtual void onMessageReceived(IChannelDriver* driver,
			ClientDetails* clientDetails, StyxMessage* message) = 0;
	virtual void onMessageTransmited(IChannelDriver* driver,
			ClientDetails* clientDetails, StyxMessage* message) = 0;
	virtual void onException(IChannelDriver* driver, StyxException *error) = 0;
};

#endif // ICHANNEL_DRIVER_H_
