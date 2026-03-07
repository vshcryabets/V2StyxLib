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

    SizeResult ChannelUsbUart::sendBuffer(
        ClientId clientId,
        const StyxBuffer buffer,
        Size size)
    {
        (void)clientId;
        uint8_t u16buffer[2] = {0};

        // if streaming mode is enabled, send SOF markers before the data
        if (config.config & V2STYXLIB_STREAMING_MODE) {
            u16buffer[0] = sofMarker1;
            u16buffer[1] = sofMarker2;
            fwrite(u16buffer, 1, 2, stdout);
        }

        // write size header; if CRC16 is enabled, include 2 extra bytes for CRC
        uint8_t packetSizeBuffer[4] = {0};
        Size headerPayloadSize = (config.config & V2STYXLIB_SEND_CRC16) ? size + 2 : size;
        SizeResult headerSize = setPacketSize(
            packetSizeHeader,
            packetSizeBuffer,
            sizeof(packetSizeBuffer),
            headerPayloadSize);
        if (!headerSize.has_value()) {
            return headerSize;
        }
        fwrite(packetSizeBuffer, 1, headerSize.value(), stdout);

        // if CRC16 is enabled, calculate and send CRC16 before the data
        if (config.config & V2STYXLIB_SEND_CRC16) {
            uint16_t crc = v2styxlib_crc16_calculate(buffer, size);
            u16buffer[0] = (crc >> 8) & 0xFF;
            u16buffer[1] = crc & 0xFF;
            fwrite(u16buffer, 1, 2, stdout);
        }

        // write buffer
        ssize_t result = fwrite(buffer, 1, size, stdout);
        fflush(stdout);
        return SizeResult(static_cast<Size>(result));
    }
}