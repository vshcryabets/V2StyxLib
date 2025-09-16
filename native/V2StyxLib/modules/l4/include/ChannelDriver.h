#pragma once
#include "data.h"

namespace styxlib
{
    class ChannelRx
    {
    public:
        virtual ~ChannelRx() = default;
    };

    class ChannelTx
    {
    public:
        virtual ~ChannelTx() = default;
        virtual Size sendBuffer(const StyxBuffer buffer, Size size) = 0;
    };

    typedef std::shared_ptr<ChannelRx> ChannelRxPtr;
    typedef std::shared_ptr<ChannelTx> ChannelTxPtr;
}