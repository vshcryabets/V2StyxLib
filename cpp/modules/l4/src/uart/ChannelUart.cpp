#include "uart/ChannelUart.h"

namespace styxlib
{

ChannelUart::ChannelUart(const ChannelUartConfig* config)
    : config(config)
{
}
    
ChannelUartConfig::ChannelUartConfig(
    uint8_t config,
    PacketHeaderSize packetSizeHeader,
    uint32_t baudrate
): 
    V2styxlibUartConfig{.baseConfig = {
        .config = config,
        .sof = {V2STYXLIB_SOF_MARKER_1, V2STYXLIB_SOF_MARKER_2}
    }},
    packetSizeHeader(packetSizeHeader),
    baudrate(baudrate)
{
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

    if (config->baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) {
        auto sofResult = internalSendBytes(const_cast<uint8_t*>(config->baseConfig.sof), 2);
        if (!sofResult.has_value()) {
            return sofResult;
        }
    }

    const Size payloadSize = (config->baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16)
        ? static_cast<Size>(size + 2)
        : size;
    const Size headerBytes = static_cast<Size>(config->packetSizeHeader);
    auto headerSize = setPacketSize(
        config->packetSizeHeader,
        packetSizeBuffer,
        static_cast<Size>(sizeof(packetSizeBuffer)),
        payloadSize + headerBytes);
    if (!headerSize.has_value()) {
        return Unexpected(headerSize.error());
    }

    auto headerResult = internalSendBytes(packetSizeBuffer, headerSize.value());
    if (!headerResult.has_value()) {
        return headerResult;
    }

    auto dataResult = internalSendBytes(buffer, size);
    if (!dataResult.has_value()) {
        return dataResult;
    }

    if (config->baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) {
        const uint16_t crc = v2styxlib_crc16_calculate(buffer, size);
        const uint8_t crcBuffer[2] = {
            static_cast<uint8_t>((crc >> 8) & 0xFF),
            static_cast<uint8_t>(crc & 0xFF)
        };
        auto crcResult = internalSendBytes(const_cast<uint8_t*>(crcBuffer), sizeof(crcBuffer));
        if (!crcResult.has_value()) {
            return crcResult;
        }
    }
    return dataResult;
}

}