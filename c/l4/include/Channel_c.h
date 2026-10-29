#pragma once

#include "defines.h"
#include "arch.h"

#ifdef __cplusplus
extern "C" {
#endif

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
uint16_t v2styxlib_crc16_calculate(
    const uint8_t *data, 
    BufferSize_t length
);

/**
 * buffer - is a pointer to the data buffer that contains the data to be sent over UART.
 * length - is the number of bytes to be sent from the buffer.
 * This function is responsible for sending a specified number of bytes from the provided buffer over UART.
 */
void v2styxlib_uart_send(
    const V2styxlibUartConfig* config,
    const uint8_t *buffer,
    BufferSize_t length);

/**
 * Encode a buffer using Consistent Overhead Byte Stuffing (COBS).
 * 
 * The function encodes the input buffer in-place by inserting code bytes that indicate
 * the distance to the next occurrence of the protected byte.
 * This allows the protected byte to be used as a frame delimiter without ambiguity.
 *
 * NOTE: buffer[0] is reserved for the first COBS code byte. Input data is at buffer[1..inputLen-1].
 * The caller should ensure the buffer has sufficient space (at least inputLen bytes).
 *
 * buffer - pointer to the buffer to encode (modified in-place). buffer[0] is reserved for first code byte.
 * inputLen - total size of the buffer including the reserved byte (at least 2). Input data is at buffer[1..inputLen-1].
 * protectedByte - the byte value that should not appear in the encoded output (typically 0x00)
 *
 * Returns the length of the encoded buffer (including the reserved code byte at buffer[0]), or a negative error code on failure.
 * Returns COBS_ENCODE_NULL_BUFFER if buffer is null.
 * Returns COBS_ENCODE_INPUT_TOO_SMALL if inputLen < 2 (need at least 1 byte reserved + 1 byte data).
 * Returns COBS_ENCODE_INPUT_TOO_LARGE if inputLen > 254.
 * Returns COBS_ENCODE_OUTPUT_TOO_LARGE if encoded output exceeds 255 bytes.
 *
 * Note: The encoded buffer may be larger than the input buffer by up to 1/253rd of its size.
 * Ensure the output buffer has sufficient space (max 255 bytes).
 */
int16_t v2styxlib_cobs_encode(uint8_t *buffer, BufferSize_t inputLen, uint8_t protectedByte);

#ifdef __cplusplus
}
#endif
