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
    bool sendCrc16
) {
    if (useStreamingMode) {
        config->config |= V2STYXLIB_STREAMING_MODE;
    } else {
        config->config &= ~V2STYXLIB_STREAMING_MODE;
    }
    if (sendCrc16) {
        config->config |= V2STYXLIB_SEND_CRC16;
    } else {
        config->config &= ~V2STYXLIB_SEND_CRC16;
    }
}