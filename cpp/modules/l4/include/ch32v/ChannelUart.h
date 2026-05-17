#pragma once

#include "uart/ChannelUart.h"

namespace styxlib
{

class ChannelCh32Uart: public ChannelUart
{
protected:
    SizeResult internalSendBytes(
        const StyxBuffer buffer, 
        Size size) override;
public:
    ChannelCh32Uart(
        const ChannelUartConfig* config
    );
    virtual ~ChannelCh32Uart();
    ErrorCode configureUart() override;
};

}