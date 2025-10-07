#pragma once
#include "messages/base/StyxMessage.h"
#include "Channel.h"
#include "SerializerL4.h"

namespace styxlib
{
    class Serializer: public SerializerL4
    {
    public:
        Serializer(ChannelTxPtr channelTx) : SerializerL4(channelTx) {}
        virtual ~Serializer() = default;
        virtual Tag sendMessage(const messages::base::StyxMessage &message) = 0;
    };
}