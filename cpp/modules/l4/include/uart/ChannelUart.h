#pragma once

#include "ChannelRx.h"
#include "ChannelTx.h"

namespace styxlib
{

class ChannelUartConfig: public V2styxlibUartConfig
{
public:
    bool useStreamingMode = true;
    uint8_t sofMarkers[2] = {V2STYXLIB_SOF_MARKER_1, V2STYXLIB_SOF_MARKER_2};
    bool sendCrc16 = true;
    PacketHeaderSize packetSizeHeader = PacketHeaderSize::Size2Bytes;
public:
    ChannelUartConfig(uint8_t config);
};

class ChannelUart : public ChannelRx, public ChannelTx {
public:
    ChannelUart(const ChannelUartConfig* config);
    ~ChannelUart() = default;
    SizeResult sendBuffer(
        ClientId clientId, 
        const StyxBuffer buffer, 
        Size size) override;
protected:
    const ChannelUartConfig* config;
    virtual SizeResult internalSendBytes(
        const uint8_t* buffer, 
        Size size) = 0;
};

}