#pragma once

#include "uart/ChannelUart.h"

namespace styxlib
{

class ChannelEsp32Uart: public ChannelUart
{
protected:
    SizeResult internalSendBytes(
        const StyxBuffer buffer, 
        Size size) override;
public:
    ChannelEsp32Uart(
        const ChannelUartConfig* config
    );
    virtual ~ChannelEsp32Uart();
    ErrorCode configureUart() override;
};

}