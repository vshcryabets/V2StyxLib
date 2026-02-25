#include "ChannelUartStm8.h"

#define WAIT_FOR_TXE() while (!(UART1->SR & UART1_SR_TXE))

uint16_t v2styxlib_uart_crc16_update(uint16_t crc, uint8_t data) {
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

uint16_t v2styxlib_uart_crc16_calculate(const uint8_t *data, BufferSize_t length) {
    uint16_t crc = V2STYXLIB_CRC16_INITIAL_VALUE;
    for (BufferSize_t i = 0; i < length; i++) {
        crc = v2styxlib_uart_crc16_update(crc, data[i]);
    }
    return crc;
}

void v2styxlib_uart_configure_proto(
    V2styxlibUartConfig *config,
    bool useStreamingMode
) {
    if (useStreamingMode) {
        config->config |= V2STYXLIB_STREAMING_MODE;
    } else {
        config->config &= ~V2STYXLIB_STREAMING_MODE;
    }
}

void v2styxlib_uart_setup(uint16_t baudRateDivider) 
{
    UART1->BRR2 = ((baudRateDivider >> 8) & 0xF0) | (baudRateDivider & 0x0F);
    UART1->BRR1 = (baudRateDivider >> 4) & 0xFF;
    UART1->CR2 |= UART1_CR2_TEN;
}

void v2styxlib_uart_send(
    V2styxlibUartConfig* config, 
    uint8_t *buffer, 
    BufferSize_t length)
{
    if (config->config & V2STYXLIB_STREAMING_MODE) {
        // If streaming mode is enabled, send SOF markers before the data
        WAIT_FOR_TXE();
        UART1->DR = V2STYXLIB_SOF_MARKER_1;

        WAIT_FOR_TXE();
        UART1->DR = V2STYXLIB_SOF_MARKER_2;
    }

    // send packet size
    WAIT_FOR_TXE();
    UART1->DR = length;

    // then CRC16
    uint16_t crc = v2styxlib_uart_crc16_calculate(buffer, length);
    WAIT_FOR_TXE();
    UART1->DR = (crc >> 8) & 0xFF; // send high byte of CRC
    WAIT_FOR_TXE();
    UART1->DR = crc & 0xFF; // send low byte of CRC

    // then send the actual data
    for (BufferSize_t i = 0; i < length; i++) {
        WAIT_FOR_TXE();
        UART1->DR = buffer[i];
    }
}