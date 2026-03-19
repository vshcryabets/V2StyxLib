#include "ChannelUartStm8.h"

#ifdef V2STYXLIB_SOFTUART
void v2styxlib_delay_bit() {
    __asm
        pushw x         
        ldw x, #535 ; hardcode for 9600 baud at 16 MHz, adjust if needed
    00001$:
        decw x  
        jrne 00001$  
        popw x  
    __endasm;
}
#endif

void v2styxlib_send_byte(
    const V2styxlibUartStm8Config* config,
    uint8_t byte) {
#ifdef V2STYXLIB_SOFTUART
    if (config->baseConfig.config & V2STYXLIB_CONFIG_SOFT_UART_TX) {
        // Send byte using software UART
        // Start bit
        config->softUartPort->ODR &= ~config->softUartTxPinMask;
        v2styxlib_delay_bit();

        // Data bits (LSB first)
        for (uint8_t i = 0; i < 8; i++) {
            if (byte & 0x01) {
                config->softUartPort->ODR |= config->softUartTxPinMask;
            } else {
                config->softUartPort->ODR &= ~config->softUartTxPinMask;
            }
            byte >>= 1;
            v2styxlib_delay_bit();
        }

        // Stop bit
        config->softUartPort->ODR |= config->softUartTxPinMask;
        v2styxlib_delay_bit();
    } else
#endif
    {    
        while (!(UART1->SR & UART1_SR_TXE)) {

        };
        UART1->DR = byte;
    }
}

void v2styxlib_uart_setup(
    const V2styxlibUartStm8Config* config,`
    uint16_t baudRateDivider) 
{
    UART1->CR1 |= UART1_CR1_UARTD; // Disable UART after configuration
    UART1->BRR2 = ((baudRateDivider >> 8) & 0xF0) | (baudRateDivider & 0x0F);
    UART1->BRR1 = (baudRateDivider >> 4) & 0xFF;
#ifdef V2STYXLIB_SOFTUART
     if (config->baseConfig.config & V2STYXLIB_CONFIG_SOFT_UART_TX) {
        // Configure the soft UART TX pin as output
        config->softUartPort->DDR |= config->softUartTxPinMask;
        config->softUartPort->CR1 |= config->softUartTxPinMask;
        config->softUartPort->CR2 |= config->softUartTxPinMask;
        config->softUartPort->ODR |= config->softUartTxPinMask;
        UART1->CR2 &= ~UART1_CR2_TEN; // Disable hardware UART transmission, we will use software UART for TX
        UART1->CR2 |= UART1_CR2_REN;
    } else 
#endif    
    {
        UART1->CR2 |= (UART1_CR2_TEN | UART1_CR2_REN);
    }
    UART1->CR1 &= ~UART1_CR1_UARTD; // Enable UART before configuration    
}

void v2styxlib_uart_send(
    const V2styxlibUartStm8Config* config, 
    const uint8_t *buffer, 
    BufferSize_t length)
{
    if (config->baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) {
        // If streaming mode is enabled, send SOF markers before the data
        v2styxlib_send_byte(config, V2STYXLIB_SOF_MARKER_1);
        v2styxlib_send_byte(config, V2STYXLIB_SOF_MARKER_2);
    }

    if (config->baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) {
        // send packet size + 2 bytes for CRC16
        v2styxlib_send_byte(config, length + 2);
        // then CRC16
        uint16_t crc = v2styxlib_crc16_calculate(buffer, length);
        v2styxlib_send_byte(config, (crc >> 8) & 0xFF); // send high byte of CRC
        v2styxlib_send_byte(config, crc & 0xFF); // send low byte of CRC
    } else {
        // send packet size
        v2styxlib_send_byte(config, length);
    }

    // then send the actual data
    for (BufferSize_t i = 0; i < length; i++) {
        v2styxlib_send_byte(config, buffer[i]);
    }
}