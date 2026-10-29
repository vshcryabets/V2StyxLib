#pragma once

#include "messages/base/StyxMessage.h"
#include "ChannelRx.h"
#include "ChannelTx.h"

namespace styxlib
{

class Client {
public:
    Client();
    virtual ~Client();
    virtual bool isConnected() const = 0;
};

} // namespace styxlib