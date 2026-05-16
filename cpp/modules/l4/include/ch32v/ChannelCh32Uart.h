#pragma once

#include "uart/ChannelUart.h"

namespace styxlib
{

class ChannelCh32Uart: public ChannelUart
{
public:
    ChannelCh32Uart(const ChannelUartConfig* config);
    virtual ~ChannelCh32Uart();
};

}