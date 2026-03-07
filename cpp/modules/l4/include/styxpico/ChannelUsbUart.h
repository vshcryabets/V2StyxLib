#pragma once

#include "Channel_c.h"
#include "ChannelTx.h"
#include "ChannelRx.h"

namespace styxlib
{
    class ChannelUsbUart : public ChannelTx, public ChannelRx
    {
    private:
        PacketHeaderSize packetSizeHeader;
        V2styxlibUartConfig config;
        uint8_t sofMarker1;
        uint8_t sofMarker2;
    public:
        /**
        * Constructor for the ChannelUsbUart class.
        * @param packetSizeHeader - The size of the packet header.
        * @param useStreamingMode - Boolean flag to indicate whether to use streaming mode.
        * @param sofMarker1 - The first Start of Frame (SOF) marker byte.
        * @param sofMarker2 - The second Start of Frame (SOF) marker byte
        * @param sendCrc16 - Boolean flag to indicate whether to calculate and send CRC16 checksum with each message.
        */
        ChannelUsbUart(
            const PacketHeaderSize &packetSizeHeader,
            bool useStreamingMode = true,
            const uint8_t sofMarker1 = V2STYXLIB_SOF_MARKER_1,
            const uint8_t sofMarker2 = V2STYXLIB_SOF_MARKER_2,
            bool sendCrc16 = true
        );
        virtual ~ChannelUsbUart();
        SizeResult sendBuffer(ClientId clientId, const StyxBuffer buffer, Size size) override;
    };
}