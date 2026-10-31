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
 * the number of data bytes to skip before the next protected byte (0x00).
 * This allows 0x00 to be used as a frame delimiter without ambiguity.
 *
 * NOTE: buffer[0] is reserved for the first COBS code byte. Input data is at buffer[1..inputLen-1].
 * The caller should ensure the buffer has sufficient space (at least inputLen bytes).
 * The protected byte is always 0x00.
 *
 * buffer - pointer to the buffer to encode (modified in-place). buffer[0] is reserved for first code byte.
 * inputLen - total size of the buffer including the reserved byte (at least 2). Input data is at buffer[1..inputLen-1].
 *
 * Returns 0 on success, or a negative error code on failure.
 * Returns COBS_ENCODE_NULL_BUFFER if buffer is null.
 * Returns COBS_ENCODE_INPUT_TOO_SMALL if inputLen < 2 (need at least 1 byte reserved + 1 byte data).
 * Returns COBS_ENCODE_INPUT_TOO_LARGE if inputLen > 254.
 * Returns COBS_ENCODE_OUTPUT_TOO_LARGE if encoded output exceeds 255 bytes.
 * Returns COBS_ENCODE_PROTECTED_BYTE_IN_DATA if input data contains 0x00.
 *
 * Note: The encoded buffer may be larger than the input buffer by up to 1/253rd of its size.
 * Ensure the output buffer has sufficient space (max 255 bytes).
 */
int8_t v2styxlib_cobs_encode(uint8_t *buffer, BufferSize_t inputLen);

/**
 * Decode a buffer that was encoded using Consistent Overhead Byte Stuffing (COBS).
 * 
 * The function reverses the COBS encoding by reading code bytes (which indicate the number
 * of data bytes to skip) and restoring the protected bytes (0x00) to their original positions.
 *
 * NOTE: The encoded data occupies buffer[0..encodedLen-1]. After decoding, decoded data
 * is written to buffer[1..encodedLen-1]. buffer[0] retains the first COBS code byte.
 * The caller can read the decoded data from buffer[1] and knows the decoded length is
 * encodedLen - 1 (the COBS code byte at buffer[0] is not needed by the consumer).
 *
 * buffer - pointer to the encoded buffer (modified in-place). Encoded data at buffer[0..encodedLen-1].
 *          After decoding, decoded data is at buffer[1..encodedLen-1].
 * encodedLen - total size of the encoded buffer (the output length from encoding).
 *
 * Returns 0 on success, or a negative error code on failure.
 * Returns COBS_ENCODE_NULL_BUFFER if buffer is null.
 * Returns COBS_ENCODE_INPUT_TOO_SMALL if encodedLen < 2 (need at least code byte + data).
 * Returns COBS_ENCODE_INPUT_TOO_LARGE if encodedLen > 255.
 * Returns COBS_ENCODE_PROTECTED_BYTE_IN_DATA if a code byte is 0x00 (malformed encoding).
 *
 * Note: The protected byte is always 0x00. The decoder validates that no code byte
 * is 0x00 (which would indicate data corruption).
 */
int8_t v2styxlib_cobs_decode_0(uint8_t *buffer, BufferSize_t encodedLen);

#ifdef __cplusplus
}
#endif
