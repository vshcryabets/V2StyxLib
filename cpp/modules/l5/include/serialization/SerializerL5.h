#pragma once
#include "messages/base/StyxMessage.h"
#include "ChannelTx.h"
#include "SerializerL4.h"

namespace styxlib
{
    class SerializerL5 : public SerializerL4
    {
    public:
        SerializerL5(ChannelTxPtr channelTx) : SerializerL4(channelTx) {}
        virtual ~SerializerL5() = default;
        virtual Tag sendMessage(const messages::base::StyxMessage &message) = 0;
    };
}