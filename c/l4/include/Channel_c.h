#pragma once

#include <stdint.h>
#include <stdbool.h>

#ifndef BufferSize_t
    #define BufferSize_t uint8_t
#endif

#ifndef V2STYXLIB_CRC16_POLY
    #define V2STYXLIB_CRC16_POLY 0x1021
#endif
#ifndef V2STYXLIB_CRC16_INITIAL_VALUE
    #define V2STYXLIB_CRC16_INITIAL_VALUE 0xFFFF
#endif

#define V2STYXLIB_CONFIG_STREAMING_MODE 0x01
#define V2STYXLIB_CONFIG_SEND_CRC16 0x02
#define V2STYXLIB_CONFIG_SOFT_UART_TX 0x04

#ifndef V2STYXLIB_SOF_MARKER_1
    #define V2STYXLIB_SOF_MARKER_1 0x55
    #define V2STYXLIB_SOF_MARKER_2 0xAA
#endif

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    uint8_t config;
} V2styxlibUartConfig;

/**
 * useStreamingMode - is a boolean flag that indicates whether to
 * use streaming mode for UART communication.
 * In streaming mode client will not wait for the response after
 * sending a message, but will continue to send messages without blocking.
 * We will send SOF marker 0x55 0xAA (check V2STYXLIB_SOF_MARKER_xx) before each frame.
 *
 * sendCrc16 - is a boolean flag that indicates whether to calculate and send
 * CRC16 checksum with each message.
 *
 * useSoftUartTx - is a boolean flag that indicates whether to use software 
 * UART for transmission.
 */
void v2styxlib_uart_configure_proto(
    V2styxlibUartConfig *config,
    bool useStreamingMode,
    bool sendCrc16,
    bool useSoftUartTx
);

/**
 * Calculate the CRC16 checksum for the given data.
 *
 * data - pointer to the data buffer
 * length - length of the data buffer
 *
 * Returns the calculated CRC16 checksum.
 */
uint16_t v2styxlib_crc16_calculate(const uint8_t *data, BufferSize_t length);

#ifdef __cplusplus
}
#endif
