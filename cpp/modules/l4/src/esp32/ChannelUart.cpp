#include "esp32/ChannelUart.h"

namespace styxlib
{

ChannelEsp32Uart::ChannelEsp32Uart(const ChannelUartConfig* config)
    : ChannelUart(config)
{
}

ChannelEsp32Uart::~ChannelEsp32Uart()
{
}

ErrorCode ChannelEsp32Uart::configureUart()
{
    return ErrorCode::NotConnected;
}

SizeResult ChannelEsp32Uart::internalSendBytes(
    const StyxBuffer buffer, 
    Size size
) {
    return 0;
}

}