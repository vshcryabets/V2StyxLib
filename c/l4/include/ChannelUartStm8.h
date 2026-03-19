#pragma once

#include <stdint.h>
#include "stm8s.h"

#include "Channel_c.h"

typedef struct {
    V2styxlibUartConfig baseConfig;
#ifdef V2STYXLIB_SOFTUART    
    GPIO_TypeDef * softUartPort;
    uint8_t softUartTxPinMask;
#endif    
} V2styxlibUartStm8Config;

/**
 * baudRateDivider - is a value that determines the data transmission speed over UART.
 * It is calculated based on the clock frequency (F_CPU) and the desired transmission speed (baud rate).
 * The formula for calculating baudRateDivider for STM8 is as follows:
 * baudRateDivider = F_CPU / baud_rate
 * For example, for F_CPU = 16MHz and baud_rate = 115200, the calculation will be as follows:
 * baudRateDivider = 16000000 / 115200 ≈ 138.89
 * In this case, baudRateDivider will be approximately 139 (rounded to the nearest integer).
 */
void v2styxlib_uart_setup(
    const V2styxlibUartStm8Config* config,
    uint16_t baudRateDivider
    );

/**
 * buffer - is a pointer to the data buffer that contains the data to be sent over UART.
 * length - is the number of bytes to be sent from the buffer.
 * This function is responsible for sending a specified number of bytes from the provided buffer over UART.
 */
void v2styxlib_uart_send(
    const V2styxlibUartStm8Config* config, 
    const uint8_t *buffer, 
    BufferSize_t length
);
