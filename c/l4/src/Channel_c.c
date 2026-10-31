#include "Channel_c.h"

static inline uint16_t v2styxlib_crc16_update(uint16_t crc, uint8_t data) {
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

uint16_t v2styxlib_crc16_calculate(const uint8_t *data, BufferSize_t length) {
    uint16_t crc = V2STYXLIB_CRC16_INITIAL_VALUE;
    for (BufferSize_t i = 0; i < length; i++) {
        crc = v2styxlib_crc16_update(crc, data[i]);
    }
    return crc;
}

void v2styxlib_uart_configure_proto(
    V2styxlibUartConfig *config,
    bool useStreamingMode,
    bool sendCrc16,
    bool useSoftUartTx
) {
    if (useStreamingMode) {
        config->baseConfig.config |= V2STYXLIB_CONFIG_STREAMING_MODE;
        config->baseConfig.sof[0] = V2STYXLIB_SOF_MARKER_1;
        config->baseConfig.sof[1] = V2STYXLIB_SOF_MARKER_2;
    } else {
        config->baseConfig.config &= ~V2STYXLIB_CONFIG_STREAMING_MODE;
    }
    if (sendCrc16) {
        config->baseConfig.config |= V2STYXLIB_CONFIG_SEND_CRC16;
    } else {
        config->baseConfig.config &= ~V2STYXLIB_CONFIG_SEND_CRC16;
    }
    if (useSoftUartTx) {
        config->baseConfig.config |= V2STYXLIB_CONFIG_SOFT_UART_TX;
    } else {
        config->baseConfig.config &= ~V2STYXLIB_CONFIG_SOFT_UART_TX;
    }
}

void v2styxlib_uart_send(
    const V2styxlibUartConfig* config, 
    const uint8_t *buffer, 
    BufferSize_t length)
{
    if (config->baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) {
        // If streaming mode is enabled, send SOF markers before the data
        v2styxlib_uart_send_bytes(config, config->baseConfig.sof, 2);
    }

    {
        uint8_t packetSize = length;
        if (config->baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) {
            // send packet size + 2 bytes for CRC16
            packetSize = (uint8_t)(packetSize + 2);
        }
        v2styxlib_uart_send_bytes(config, &packetSize, 1);
    }

    // then send the actual data
    v2styxlib_uart_send_bytes(config, buffer, length);

    if (config->baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) {
        // send CRC16 after data
        uint16_t crc = v2styxlib_crc16_calculate(buffer, length);
        uint8_t crcBytes[2] = {
            (uint8_t)((crc >> 8) & 0xFF),
            (uint8_t)(crc & 0xFF)
        };
        v2styxlib_uart_send_bytes(config, crcBytes, 2);
    }
}

/**
 * @brief Encodes a buffer using Consistent Overhead Byte Stuffing (COBS) protecting 0x00.
 *
 * buffer[0] is reserved for the first COBS code byte.
 * Input data is at buffer[1..inputLen-1].
 * The encoded output is written back to buffer[0..inputLen-1] or buffer[0..inputLen] depending on encoding.
 *
 * @param buffer The buffer to encode. buffer[0] is reserved. Input data at buffer[1..inputLen-1].
 * @param inputLen The length of the input data (number of bytes at buffer[1..inputLen-1]).
 * @return int8_t 0 on success, or a negative error code on failure.
 */
int8_t v2styxlib_cobs_encode_0(uint8_t *buffer, BufferSize_t inputLen) {
    if (!buffer) {
        return COBS_ENCODE_NULL_BUFFER;
    }
    
    if (inputLen < 2) {
        return COBS_ENCODE_INPUT_TOO_SMALL;
    }
    
    if (inputLen > 254) {
        return COBS_ENCODE_INPUT_TOO_LARGE;
    }

    uint16_t readIdx = 1; // Read input data from buffer[1..inputLen-1]
    uint8_t codeIdx = 0;  // Current code byte position
    uint8_t code = 1;     // Distance to next protected byte (or end)

    while (readIdx < inputLen) {
        if (buffer[readIdx] == 0x00) {
            buffer[codeIdx] = code;
            codeIdx = (uint8_t)readIdx;
            code = 1;
        } else {
            code++;
        }
        readIdx++;
    }

    buffer[codeIdx] = code;
    return 0;
}

/**
 * @brief Decodes a buffer that was encoded using Consistent Overhead Byte Stuffing (COBS).
 *
 * Reads code bytes and restores the protected bytes (0x00) to their original positions.
 *
 * @param buffer The encoded buffer (modified in-place). Contains encoded data at buffer[0..encodedLen-1].
 * @param encodedLen The length of the encoded data.
 * @return int8_t 0 on success, or a negative error code on failure.
 */
int8_t v2styxlib_cobs_decode_0(uint8_t *buffer, BufferSize_t encodedLen) {
    if (!buffer) {
        return COBS_ENCODE_NULL_BUFFER;
    }
    
    if (encodedLen < 2) {
        return COBS_ENCODE_INPUT_TOO_SMALL;
    }
    
    if (encodedLen > 255) {
        return COBS_ENCODE_INPUT_TOO_LARGE;
    }

    uint16_t writeIdx = buffer[0];
    while (writeIdx < encodedLen) {
        uint8_t code = buffer[writeIdx];
        if (code == 0x00) {
            return COBS_ENCODE_PROTECTED_BYTE_IN_DATA;
        }
        buffer[writeIdx] = 0x00;
        writeIdx += code;
    }
    return 0;
}
/**
 * @brief Encodes a buffer using COBS with a custom protected byte.
 *
 * Wrapper around v2styxlib_cobs_encode_0 that supports any protected byte value.
 * Transforms the problem by XORing data with the protected byte.
 *
 * @param buffer The buffer to encode (modified in-place). buffer[0] is reserved.
 * @param inputLen The length of input data (including reserved byte[0]).
 * @param protectedByte The byte value to protect (typically 0x00 for UART framing).
 * @return int8_t 0 on success, or a negative error code on failure.
 */
int8_t v2styxlib_cobs_encode(uint8_t *buffer, BufferSize_t inputLen, uint8_t protectedByte) {
    if (!buffer) {
        return COBS_ENCODE_NULL_BUFFER;
    }
    
    if (inputLen < 2 || inputLen > 254) {
        return inputLen < 2 ? COBS_ENCODE_INPUT_TOO_SMALL : COBS_ENCODE_INPUT_TOO_LARGE;
    }

    // Encode using the 0x00 encoder (input may contain 0x00 as regular data)
    int8_t result = v2styxlib_cobs_encode_0(buffer, inputLen);
    
    if (result != 0) {
        return result;
    }

    // XOR output buffer (code and data bytes) with protected byte
    // This transforms protectedByte to the 0x00
    for (uint16_t i = 0; i < inputLen; i++) {
        buffer[i] ^= protectedByte;
    }

    return 0;
}

/**
 * @brief Decodes a buffer that was encoded using COBS with a custom protected byte.
 *
 * Wrapper around v2styxlib_cobs_decode_0 that supports any protected byte value.
 *
 * @param buffer The encoded buffer (modified in-place). Encoded data at buffer[0..encodedLen-1].
 * @param encodedLen The length of the encoded data.
 * @param protectedByte The byte value that was protected during encoding.
 * @return int8_t 0 on success, or a negative error code on failure.
 */
int8_t v2styxlib_cobs_decode(uint8_t *buffer, BufferSize_t encodedLen, uint8_t protectedByte) {
    if (!buffer) {
        return COBS_ENCODE_NULL_BUFFER;
    }
    
    if (encodedLen < 2 || encodedLen > 255) {
        return encodedLen < 2 ? COBS_ENCODE_INPUT_TOO_SMALL : COBS_ENCODE_INPUT_TOO_LARGE;
    }

    // XOR return buffer with protectedBytes
    for (uint16_t i = 0; i < encodedLen; i++) {
        buffer[i] ^= protectedByte;
    }

    // Decode using the 0x00 decoder
    return v2styxlib_cobs_decode_0(buffer, encodedLen);
}
