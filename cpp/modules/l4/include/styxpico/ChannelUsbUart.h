#pragma once

#include "Channel_c.h"
#include "uart/ChannelUart.h"

namespace styxlib
{
    class ChannelUsbUart : public ChannelUart
    {
    public:
        ChannelUsbUart(const ChannelUartConfig* config);
        virtual ~ChannelUsbUart();
        SizeResult sendBuffer(
            ClientId clientId, 
            const StyxBuffer buffer, 
            Size size) override;
    };
}