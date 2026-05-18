#include "styxpico/ChannelUsbUart.h"
#include "pico/stdlib.h"

namespace styxlib
{

ChannelUsbUart::ChannelUsbUart(
    const PacketHeaderSize &packetSizeHeader,
    bool useStreamingMode,
    const uint8_t sofMarker1,
    const uint8_t sofMarker2,
    bool sendCrc16)
    : packetSizeHeader(packetSizeHeader)
    , sofMarker1(sofMarker1)
    , sofMarker2(sofMarker2)
{
    config.config = 0;
    v2styxlib_uart_configure_proto(&config, 
        useStreamingMode,
        sendCrc16);
}

ChannelUsbUart::~ChannelUsbUart()
{
}

SizeResult ChannelUsbUart::internalSendBytes(
    const StyxBuffer buffer, 
    Size size)
{
    return SizeResult(fwrite(buffer, 1, size, stdout));
    
}

}