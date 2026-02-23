#pragma once
#include "data.h"

namespace styxlib
{
    class SerializerL4
    {
    protected:
        ChannelTxPtr channelTx;

    public:
        SerializerL4(ChannelTxPtr channelTx) : channelTx(channelTx) {}
        virtual ~SerializerL4() = default;
    };

    class DeserializerL4
    {
    public:
        DeserializerL4() = default;
        virtual ~DeserializerL4() = default;
        virtual void handleBuffer(ClientId clientId, const StyxBuffer buffer, Size size) = 0;
    };
}