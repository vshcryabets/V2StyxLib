#pragma once

#include "defines.h"
#include "structs.h"

#include "driver/uart.h"

#ifdef __cplusplus
extern "C" {
#endif

enum v2styxlib_esp32_error_code {
    V2STYXLIB_ESP32_ERROR_NONE = 0,
    V2STYXLIB_ESP32_ERROR_NULLPTR_ARGUMENT = -1,
    V2STYXLIB_ESP32_ERROR_CONFIGURE_FAILED = -2
};

typedef struct {
    V2styxlibUartConfigBase baseConfig;
    uint32_t _baudrate;
    uart_port_t _uartPortNum;
    int _txPin;
    int _rxPin;
    uint16_t _rxBufferSize = 256;    
} V2styxlibUartEsp32vConfig;

/**
 * Configure ESP32 UART peripheral with the given configuration.
 * Must be called once before sending data.
 */
int8_t v2styxlib_uart_esp32_setup(const V2styxlibUartEsp32vConfig* config);

/**
 * Send multiple bytes over ESP32 UART.
 */
BufferSize_t v2styxlib_uart_esp32_send_bytes(
    const V2styxlibUartEsp32vConfig* config,
    const uint8_t* buffer,
    BufferSize_t length);

#ifdef __cplusplus
}
#endif
