#include "uart/ChannelUart.h"

namespace styxlib
{

ChannelUart::ChannelUart(const ChannelUartConfig* config)
    : config(config)
{
}
    
ChannelUartConfig::ChannelUartConfig(uint8_t config)
{
    this->config = config;
    this->useStreamingMode = (config & V2STYXLIB_CONFIG_STREAMING_MODE) != 0;
    this->sendCrc16 = (config & V2STYXLIB_CONFIG_SEND_CRC16) != 0;
    this->sofMarkers[0] = (config & V2STYXLIB_CONFIG_STREAMING_MODE) != 0 ? V2STYXLIB_SOF_MARKER_1 : 0;
    this->sofMarkers[1] = (config & V2STYXLIB_CONFIG_STREAMING_MODE) != 0 ? V2STYXLIB_SOF_MARKER_2 : 0;
    this->packetSizeHeader = PacketHeaderSize::Size2Bytes; // Default packet size header
}

SizeResult ChannelUart::sendBuffer(
    ClientId clientId,
    const StyxBuffer buffer,
    Size size)
{
    (void)clientId;

    if (config == nullptr || buffer == nullptr) {
        return Unexpected(ErrorCode::NullptrArgument);
    }

    if (config->config & V2STYXLIB_CONFIG_STREAMING_MODE) {
        auto sofResult = internalSendBytes(config->sofMarkers, static_cast<Size>(2));
        if (!sofResult.has_value()) {
            return sofResult;
        }
    }

    uint8_t packetSizeBuffer[4] = { 0, 0, 0, 0 };
    const Size payloadSize = (config->config & V2STYXLIB_CONFIG_SEND_CRC16)
        ? static_cast<Size>(size + 2)
        : size;
    auto headerSize = setPacketSize(
        config->packetSizeHeader,
        packetSizeBuffer,
        static_cast<Size>(sizeof(packetSizeBuffer)),
        payloadSize);
    if (!headerSize.has_value()) {
        return Unexpected(headerSize.error());
    }

    auto headerResult = internalSendBytes(packetSizeBuffer, headerSize.value());
    if (!headerResult.has_value()) {
        return headerResult;
    }

    if (config->config & V2STYXLIB_CONFIG_SEND_CRC16) {
        const uint16_t crc = v2styxlib_crc16_calculate(buffer, size);
        const uint8_t crcBuffer[2] = {
            static_cast<uint8_t>((crc >> 8) & 0xFF),
            static_cast<uint8_t>(crc & 0xFF)
        };
        auto crcResult = internalSendBytes(crcBuffer, static_cast<Size>(sizeof(crcBuffer)));
        if (!crcResult.has_value()) {
            return crcResult;
        }
    }

    return internalSendBytes(buffer, size);
}

}