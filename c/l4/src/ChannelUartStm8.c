#include "ChannelUartStm8.h"

#define WAIT_FOR_TXE() while (!(UART1->SR & UART1_SR_TXE))

void v2styxlib_uart_setup(uint16_t baudRateDivider) 
{
    UART1->BRR2 = ((baudRateDivider >> 8) & 0xF0) | (baudRateDivider & 0x0F);
    UART1->BRR1 = (baudRateDivider >> 4) & 0xFF;
    UART1->CR2 |= UART1_CR2_TEN;
}

void v2styxlib_uart_send(
    const V2styxlibUartConfig* config, 
    const uint8_t *buffer, 
    BufferSize_t length)
{
    if (config->config & V2STYXLIB_STREAMING_MODE) {
        // If streaming mode is enabled, send SOF markers before the data
        WAIT_FOR_TXE();
        UART1->DR = V2STYXLIB_SOF_MARKER_1;

        WAIT_FOR_TXE();
        UART1->DR = V2STYXLIB_SOF_MARKER_2;
    }

    WAIT_FOR_TXE();
    if (config->config & V2STYXLIB_SEND_CRC16) {
        // send packet size + 2 bytes for CRC16
        UART1->DR = length + 2;
        // then CRC16
        uint16_t crc = v2styxlib_uart_crc16_calculate(buffer, length);
        WAIT_FOR_TXE();
        UART1->DR = (crc >> 8) & 0xFF; // send high byte of CRC
        WAIT_FOR_TXE();
        UART1->DR = crc & 0xFF; // send low byte of CRC
    } else {
        // send packet size
        UART1->DR = length;
    }

    // then send the actual data
    for (BufferSize_t i = 0; i < length; i++) {
        WAIT_FOR_TXE();
        UART1->DR = buffer[i];
    }
}