#pragma once

#include "ChannelRx.h"
#include "ChannelTx.h"

namespace styxlib
{

class ChannelUartConfig: public V2styxlibUartConfig
{
public:
    const uint8_t sofMarkers[2];
    PacketHeaderSize packetSizeHeader = PacketHeaderSize::Size2Bytes;
    uint32_t baudrate = 9600;
public:
    ChannelUartConfig(uint8_t config,
        PacketHeaderSize packetSizeHeader = PacketHeaderSize::Size2Bytes,
        uint32_t baudrate = 9600
    );
};

class ChannelUart : public ChannelRx, public ChannelTx {
public:
    ChannelUart(const ChannelUartConfig* config);
    ~ChannelUart() = default;
    /**
     * Configures the UART peripheral based on the provided configuration.
     * @return ErrorCode indicating the success or failure of the configuration.
     */
    virtual ErrorCode configureUart() = 0;
    /**
     * Sends a buffer of data. The buffer will be framed with SOF markers, 
     * a header containing the packet size, and an optional CRC16 checksum based on the configuration.
     */
    SizeResult sendBuffer(
        ClientId clientId, 
        const StyxBuffer buffer, 
        Size size) override;
protected:
    uint8_t packetSizeBuffer[4] = { 0, 0, 0, 0 };
    const ChannelUartConfig* config;
    virtual SizeResult internalSendBytes(
        const StyxBuffer buffer, 
        Size size) = 0;
};

}