#pragma once

#include "Channel_c.h"
#include "uart/ChannelUart.h"

namespace styxlib
{
    class ChannelUsbUart : public ChannelUart
    {
    protected:
        SizeResult internalSendBytes(
            const StyxBuffer buffer, 
            Size size) override;
        void configureUart() override {};
    public:
        ChannelUsbUart(const ChannelUartConfig* config);
        virtual ~ChannelUsbUart();
    };
}