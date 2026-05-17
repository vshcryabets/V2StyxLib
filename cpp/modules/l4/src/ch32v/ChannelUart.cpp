#if defined(CH32V00X)
#include <ch32v00x.h>
#elif defined(CH32V10X)
#include <ch32v10x.h>
#elif defined(CH32V20X)
#include <ch32v20x.h>
#elif defined(CH32V30X) || defined(CH32V31X)
#include <ch32v30x.h>
#elif defined(CH32L10X)
#include <ch32l103.h>
#endif

#include "ChannelUartCh32v.h"
#include "ch32v/ChannelUart.h"

namespace styxlib
{

ChannelCh32Uart::ChannelCh32Uart(const ChannelUartConfig* config)
    : ChannelUart(config)
{
}

ChannelCh32Uart::~ChannelCh32Uart()
{
}

ErrorCode ChannelCh32Uart::configureUart()
{
    v2styxlib_uart_ch32v_setup(reinterpret_cast<const V2styxlibUartCh32vConfig*>(config));
    return ErrorCode::Success;
}

SizeResult ChannelCh32Uart::internalSendBytes(
    const StyxBuffer buffer, 
    Size size
) {
    v2styxlib_uart_ch32v_send_bytes(
        reinterpret_cast<const V2styxlibUartCh32vConfig*>(config),
        buffer,
        size);
    return SizeResult(size);
}

}