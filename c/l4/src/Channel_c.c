#include "Channel_c.h"

static inline uint16_t v2styxlib_crc16_update(uint16_t crc, uint8_t data) {
    crc ^= (uint16_t)data << 8;
    for (uint8_t i = 0; i < 8; i++) {
        if (crc & 0x8000) {
            crc = (crc << 1) ^ V2STYXLIB_CRC16_POLY;
        } else {
            crc <<= 1;
        }
    }
    return crc;
}

uint16_t v2styxlib_crc16_calculate(const uint8_t *data, BufferSize_t length) {
    uint16_t crc = V2STYXLIB_CRC16_INITIAL_VALUE;
    for (BufferSize_t i = 0; i < length; i++) {
        crc = v2styxlib_crc16_update(crc, data[i]);
    }
    return crc;
}

void v2styxlib_uart_configure_proto(
    V2styxlibUartConfig *config,
    bool useStreamingMode,
    bool sendCrc16,
    bool useSoftUartTx
) {
    if (useStreamingMode) {
        config->baseConfig.config |= V2STYXLIB_CONFIG_STREAMING_MODE;
    } else {
        config->baseConfig.config &= ~V2STYXLIB_CONFIG_STREAMING_MODE;
    }
    if (sendCrc16) {
        config->baseConfig.config |= V2STYXLIB_CONFIG_SEND_CRC16;
    } else {
        config->baseConfig.config &= ~V2STYXLIB_CONFIG_SEND_CRC16;
    }
    if (useSoftUartTx) {
        config->baseConfig.config |= V2STYXLIB_CONFIG_SOFT_UART_TX;
    } else {
        config->baseConfig.config &= ~V2STYXLIB_CONFIG_SOFT_UART_TX;
    }
}

void v2styxlib_uart_send(
    const V2styxlibUartConfig* config, 
    const uint8_t *buffer, 
    BufferSize_t length)
{
    if (config->baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) {
        // If streaming mode is enabled, send SOF markers before the data
        v2styxlib_uart_send_byte(config, V2STYXLIB_SOF_MARKER_1);
        v2styxlib_uart_send_byte(config, V2STYXLIB_SOF_MARKER_2);
    }

    if (config->baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) {
        // send packet size + 2 bytes for CRC16
        v2styxlib_uart_send_byte(config, length + 2);
    } else {
        // send packet size
        v2styxlib_uart_send_byte(config, length);
    }

    // then send the actual data
    for (BufferSize_t i = 0; i < length; i++) {
        v2styxlib_uart_send_byte(config, buffer[i]);
    }

    if (config->baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) {
        // send CRC16 after data
        uint16_t crc = v2styxlib_crc16_calculate(buffer, length);
        v2styxlib_uart_send_byte(config, (crc >> 8) & 0xFF); // send high byte of CRC
        v2styxlib_uart_send_byte(config, crc & 0xFF); // send low byte of CRC
    }
}