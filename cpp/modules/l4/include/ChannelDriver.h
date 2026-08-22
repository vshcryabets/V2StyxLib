#pragma once

#include <future>

#include "ChannelRx.h"
#include "ChannelTx.h"

namespace styxlib
{
    class ChannelDriver: public ChannelRx, public ChannelTx
    {
    public:
        virtual std::future<ErrorCode> start() = 0;
        virtual std::future<void> stop() = 0;
        virtual bool isStarted() const = 0;
    };
}