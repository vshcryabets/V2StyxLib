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
 * @brief Encodes a buffer using Consistent Overhead Byte Stuffing (COBS) with a protected byte.
 *
 * buffer[0] is reserved for the first COBS code byte.
 * Input data is at buffer[1..inputLen].
 * The encoded output is written back to buffer[0..inputLen-1] or buffer[0..inputLen] depending on encoding.
 *
 * @param buffer The buffer to encode. buffer[0] is reserved. Input data at buffer[1..inputLen].
 * @param inputLen The length of the input data (number of bytes at buffer[1..inputLen]).
 * @param protectedByte The byte value that should be protected (not appear in the encoded data).
 * @return int8_t 0 on success, or a negative error code on failure.
 */
int8_t v2styxlib_cobs_encode(uint8_t *buffer, BufferSize_t inputLen, uint8_t protectedByte) {
    if (!buffer) {
        return COBS_ENCODE_NULL_BUFFER;
    }
    
    if (inputLen < 2) {
        return COBS_ENCODE_INPUT_TOO_SMALL;
    }
    
    if (inputLen > 254) {
        return COBS_ENCODE_INPUT_TOO_LARGE;
    }

    uint8_t encodedBuffer[256];
    uint16_t readIdx = 1;  // Start reading from buffer[1] (skip reserved buffer[0])
    uint16_t writeIdx = 1; // Start writing after code byte placeholder at buffer[0]
    uint8_t codeIdx = 0;   // Code byte position (buffer[0])
    uint8_t code = 1;

    while (readIdx < inputLen) {
        if (buffer[readIdx] == protectedByte) {
            // Write the code byte
            encodedBuffer[codeIdx] = code;
            code = 1;
            codeIdx = writeIdx;
            writeIdx++;
            readIdx++;
        } else {
            // Write the data byte
            encodedBuffer[writeIdx] = buffer[readIdx];
            code++;
            writeIdx++;
            readIdx++;

            if (code == 255) {
                // Code byte is full, write it and start a new one
                encodedBuffer[codeIdx] = code;
                code = 1;
                codeIdx = writeIdx;
                writeIdx++;
            }
        }
    }

    // Write the final code byte
    encodedBuffer[codeIdx] = code;
    
    if (writeIdx > 255) {
        return COBS_ENCODE_OUTPUT_TOO_LARGE;
    }

    for (uint16_t i = 0; i < writeIdx; i++) {
        buffer[i] = encodedBuffer[i];
    }

    return 0;
}

/**
 * @brief Decodes a buffer that was encoded using Consistent Overhead Byte Stuffing (COBS).
 *
 * Reads code bytes and restores the protected bytes to their original positions.
 *
 * @param buffer The encoded buffer (modified in-place). Contains encoded data at buffer[0..encodedLen-1].
 * @param encodedLen The length of the encoded data.
 * @param protectedByte The byte value that was protected during encoding.
 * @return int8_t 0 on success, or a negative error code on failure.
 */
int8_t v2styxlib_cobs_decode(uint8_t *buffer, BufferSize_t encodedLen, uint8_t protectedByte) {
    if (!buffer) {
        return COBS_ENCODE_NULL_BUFFER;
    }
    
    if (encodedLen < 2) {
        return COBS_ENCODE_INPUT_TOO_SMALL;
    }
    
    if (encodedLen > 255) {
        return COBS_ENCODE_INPUT_TOO_LARGE;
    }

    uint8_t decodedBuffer[256];
    uint16_t readIdx = 0;    // Read code bytes and data from encoded buffer
    uint16_t writeIdx = 0;   // Write decoded data
    uint8_t code = 0;
    uint16_t codeIndex = 0;  // Track position in current code segment

    while (readIdx < encodedLen) {
        if (codeIndex == 0) {
            // Read the code byte
            code = buffer[readIdx];
            readIdx++;
            codeIndex = 1;
            
            if (code == 0 || code > 255) {
                return COBS_ENCODE_OUTPUT_TOO_LARGE;
            }
        } else {
            // We're in the middle of a code segment
            if (codeIndex < code) {
                // Read a data byte
                uint8_t byte = buffer[readIdx];
                
                // Validate: no protected byte should appear in data
                if (byte == protectedByte) {
                    return COBS_ENCODE_OUTPUT_TOO_LARGE;
                }
                
                decodedBuffer[writeIdx] = byte;
                writeIdx++;
                readIdx++;
                codeIndex++;
            } else {
                // Reached the end of this code segment, insert the protected byte
                decodedBuffer[writeIdx] = protectedByte;
                writeIdx++;
                codeIndex = 0;
            }
        }
    }

    // Check if we ended in the middle of a code segment
    if (codeIndex > 1) {
        // We had started reading a segment but didn't finish
        // This means the last segment ended with the protected byte
        // which is valid
    } else if (codeIndex == 1 && readIdx == encodedLen) {
        // We read a code byte but no data follows - this might be valid
        // if code == 1, which means a protected byte at the end
        if (code != 1) {
            return COBS_ENCODE_OUTPUT_TOO_LARGE;
        }
    }

    // Write decoded data to buffer[1..], keep buffer[0] as COBS code
    // Consumer knows decoded size = encodedLen - 1
    for (uint16_t i = 0; i < writeIdx; i++) {
        buffer[i + 1] = decodedBuffer[i];
    }

    return 0;
}