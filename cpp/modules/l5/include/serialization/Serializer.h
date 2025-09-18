#pragma once
#include "messages/base/StyxMessage.h"
#include "Channel.h"

namespace styxlib
{
    class Serializer
    {
    protected:
        ChannelTxPtr channelTx;

    public:
        Serializer(ChannelTxPtr channelTx) : channelTx(channelTx) {}
        virtual ~Serializer() = default;
        virtual Tag sendMessage(const messages::base::StyxMessage &message) = 0;
    };
}