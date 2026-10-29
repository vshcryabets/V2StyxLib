#pragma once
#include "dataL4.h"

namespace styxlib
{
    class SerializerL4
    {
    protected:
        ChannelTxPtr channelTx;

    public:
        SerializerL4() : channelTx(nullptr) {}
        virtual ~SerializerL4() = default;
        void setChannelTx(ChannelTxPtr channelTx) { this->channelTx = channelTx; }
    };

    class DeserializerL4
    {
    public:
        DeserializerL4() = default;
        virtual ~DeserializerL4() = default;
        virtual ErrorCode handleBuffer(ClientId clientId, const StyxBuffer buffer, Size size) = 0;
    };
}