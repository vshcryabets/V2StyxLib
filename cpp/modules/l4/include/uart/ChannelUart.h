#pragma once

#include "ChannelRx.h"
#include "ChannelTx.h"

namespace styxlib
{

class ChannelUartConfig: public V2styxlibUartConfig
{
public:
    bool useStreamingMode = true;
    uint8_t sofMarker1 = V2STYXLIB_SOF_MARKER_1;
    uint8_t sofMarker2 = V2STYXLIB_SOF_MARKER_2;
    bool sendCrc16 = true;
    PacketHeaderSize packetSizeHeader = PacketHeaderSize::Size2Bytes;
public:
    ChannelUartConfig(uint8_t config);
};

class ChannelUart : public ChannelRx, public ChannelTx {
public:
    ChannelUart(const ChannelUartConfig* config);
    ~ChannelUart() = default;
private:
    const ChannelUartConfig* config;
};

}