#include "styxpico/ChannelUsbUart.h"
#include "pico/stdlib.h"

namespace styxlib
{
    ChannelUsbUart::ChannelUsbUart(const PacketHeaderSize &packetSizeHeader)
        : packetSizeHeader(packetSizeHeader)
    {
    }

    ChannelUsbUart::~ChannelUsbUart()
    {
    }

    SizeResult ChannelUsbUart::sendBuffer(
        ClientId clientId,
        const StyxBuffer buffer,
        Size size)
    {
        (void)clientId;
        // write size header
        uint8_t packetSizeBuffer[4] = {0};
        SizeResult headerSize = setPacketSize(
            packetSizeHeader, 
            packetSizeBuffer, 
            sizeof(packetSizeBuffer),
            size);
        if (!headerSize.has_value()) {
            return headerSize;
        }
        ssize_t result = fwrite(packetSizeBuffer, 1, headerSize.value(), stdout);

        // write buffer
        result = fwrite(buffer, 1, size, stdout);
        fflush(stdout);
        return SizeResult(static_cast<Size>(result));
    }
}