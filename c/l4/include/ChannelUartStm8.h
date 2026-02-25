#include <stdint.h>
#include "stm8s.h"

typedef uint8_t BufferSize_t;

#ifndef V2STYXLIB_CRC16_POLY
    #define V2STYXLIB_CRC16_POLY 0x1021
#endif
#ifndef V2STYXLIB_CRC16_INITIAL_VALUE
    #define V2STYXLIB_CRC16_INITIAL_VALUE 0xFFFF
#endif

#define V2STYXLIB_STREAMING_MODE 0x01

#ifndef V2STYXLIB_SOF_MARKER_1
    #define V2STYXLIB_SOF_MARKER_1 0x55
    #define V2STYXLIB_SOF_MARKER_2 0xAA
#endif

typedef struct {
    uint8_t config;    
} V2styxlibUartConfig;

/**
 * useStreamingMode - is a boolean flag that indicates whether to use streaming mode for UART communication.
 * In streaming mode client will not wait for the response after sending a message, but will continue to send messages without blocking.
 * We will send SOF marker 0x55 0xAA (check V2STYXLIB_SOF_MARKER_xx) before each frame.
 */
void v2styxlib_uart_configure_proto(
    V2styxlibUartConfig *config,
    bool useStreamingMode
);

/**
 * baudRateDivider - is a value that determines the data transmission speed over UART.
 * It is calculated based on the clock frequency (F_CPU) and the desired transmission speed (baud rate).
 * The formula for calculating baudRateDivider for STM8 is as follows:
 * baudRateDivider = F_CPU / baud_rate
 * For example, for F_CPU = 16MHz and baud_rate = 115200, the calculation will be as follows:
 * baudRateDivider = 16000000 / 115200 ≈ 138.89
 * In this case, baudRateDivider will be approximately 139 (rounded to the nearest integer).
 */
void v2styxlib_uart_setup(uint16_t baudRateDivider);

/**
 * buffer - is a pointer to the data buffer that contains the data to be sent over UART.
 * length - is the number of bytes to be sent from the buffer.
 * This function is responsible for sending a specified number of bytes from the provided buffer over UART.
 */
void v2styxlib_uart_send(
    V2styxlibUartConfig* config, 
    uint8_t *buffer, 
    BufferSize_t length
);