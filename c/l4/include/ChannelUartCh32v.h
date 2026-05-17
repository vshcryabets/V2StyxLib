#pragma once

#include "defines.h"
#include "structs.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    V2styxlibUartConfigBase baseConfig;
    uint32_t baudrate;
} V2styxlibUartCh32vConfig;

/**
 * Configure USART1 peripheral (PD5 TX, PD6 RX) with the given baud rate.
 * Must be called once before sending data.
 */
void v2styxlib_uart_ch32v_setup(const V2styxlibUartCh32vConfig* config);

/**
 * Send multiple bytes over USART1.
 */
void v2styxlib_uart_ch32v_send_bytes(
    const V2styxlibUartCh32vConfig* config,
    const uint8_t* buffer,
    BufferSize_t length);

#ifdef __cplusplus
}
#endif
