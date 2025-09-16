#pragma once
#include <cstdint>

namespace styxlib::handlers {
    
typedef int16_t ClientId;

class IMessageProcessor {
    public:
    virtual ~IMessageProcessor() = default;

    virtual void onClientRemoved(ClientId clientId) = 0;
    virtual void onClientMessage(StyxMessage message, ClientId clientId) = 0;
};

}