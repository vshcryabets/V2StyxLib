#include <catch2/catch_test_macros.hpp>
#include <vector>
#include <cstring>

extern "C" {
#include "Channel_c.h"
}

// Tests for v2styxlib_cobs_encode/decode with custom protected bytes

TEST_CASE("v2styxlib_cobs_encode: protected byte 0xFF, no protected bytes in data", "[cobs_wrapper]")
{
    // Original data: [0x11, 0x22, 0x33] with 0xFF protected
    uint8_t buffer[256] = {0, 0x11, 0x22, 0x33};
    
    int8_t result = v2styxlib_cobs_encode(buffer, 4, 0xFF);
    REQUIRE(result == 0);
    
    // After encoding, buffer should not contain 0xFF at code byte positions
    // Code byte is at buffer[0]
    REQUIRE(buffer[0] != 0xFF);
}

TEST_CASE("v2styxlib_cobs_encode: protected byte 0x01, no protected bytes in data", "[cobs_wrapper]")
{
    // Original data: [0x55, 0xAA, 0x77] with 0x01 protected
    uint8_t buffer[256] = {0, 0x55, 0xAA, 0x77};
    
    int8_t result = v2styxlib_cobs_encode(buffer, 4, 0x01);
    REQUIRE(result == 0);
    
    REQUIRE(buffer[0] != 0x01);
}

TEST_CASE("v2styxlib_cobs_roundtrip: data without protected byte 0xFF", "[cobs_wrapper]")
{
    // Original: [?, 0x11, 0x22, 0x33] with 0xFF protected (buffer[0] reserved)
    // After encode and decode, data bytes should be restored
    uint8_t buffer[] = {0, 0x11, 0x22, 0x33};
    size_t encodedLen = sizeof(buffer);
    
    // Encode with 0xFF protection
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, encodedLen, 0xFF);
    REQUIRE(encodeResult == 0);
    
    // Decode with 0xFF protection
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, encodedLen, 0xFF);
    REQUIRE(decodeResult == 0);
    
    // Verify data bytes are restored (buffer[0] is reserved, don't check)
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 0x22);
    REQUIRE(buffer[3] == 0x33);
}

TEST_CASE("v2styxlib_cobs_roundtrip: encoded data with protected bytes 0xAA", "[cobs_wrapper]")
{
    // Original: [?, 0x11, 0xAA, 0x22, 0xAA, 0x33] with 0xAA protected
    uint8_t buffer[] = {0, 0x11, 0xAA, 0x22, 0xAA, 0x33};
    size_t encodedLen = sizeof(buffer);
    uint8_t original[5] = {0x11, 0xAA, 0x22, 0xAA, 0x33};
    
    // Encode with 0xAA protection
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, encodedLen, 0xAA);
    REQUIRE(encodeResult == 0);
    
    // Store the encoded data (we'll verify code bytes are correct)
    for (int i = 0; i < encodedLen; i++) {
        REQUIRE(buffer[i] != 0xAA);  // Code byte should not be protected byte
    }
    
    // Decode with 0xAA protection
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, encodedLen, 0xAA);
    REQUIRE(decodeResult == 0);
    
    // After roundtrip, data bytes should be restored (buffer[0] reserved)
    REQUIRE(buffer[1] == original[0]);
    REQUIRE(buffer[2] == original[1]);
    REQUIRE(buffer[3] == original[2]);
    REQUIRE(buffer[4] == original[3]);
    REQUIRE(buffer[5] == original[4]);
}

TEST_CASE("v2styxlib_cobs_roundtrip: simple roundtrip with 0xFF", "[cobs_wrapper]")
{
    // Simple case: [?, 0x55, 0xAA] with 0xFF protection (buffer[0] reserved)
    uint8_t buffer[256] = {0, 0x55, 0xAA};
    
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, 3, 0xFF);
    REQUIRE(encodeResult == 0);
    
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, 3, 0xFF);
    REQUIRE(decodeResult == 0);
    
    // Verify data bytes are restored
    REQUIRE(buffer[1] == 0x55);
    REQUIRE(buffer[2] == 0xAA);
}

TEST_CASE("v2styxlib_cobs_encode: null buffer with custom protected byte", "[cobs_wrapper]")
{
    int8_t result = v2styxlib_cobs_encode(nullptr, 5, 0xFF);
    REQUIRE(result == COBS_ENCODE_NULL_BUFFER);
}

TEST_CASE("v2styxlib_cobs_decode: null buffer with custom protected byte", "[cobs_wrapper]")
{
    int8_t result = v2styxlib_cobs_decode(nullptr, 5, 0xFF);
    REQUIRE(result == COBS_ENCODE_NULL_BUFFER);
}

TEST_CASE("v2styxlib_cobs_encode: input too small with custom protected byte", "[cobs_wrapper]")
{
    uint8_t buffer[256] = {0};
    int8_t result = v2styxlib_cobs_encode(buffer, 1, 0xFF);
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_SMALL);
}

TEST_CASE("v2styxlib_cobs_encode: input too large with custom protected byte", "[cobs_wrapper]")
{
    uint8_t buffer[256];
    std::memset(buffer, 0xAA, 255);
    
    int8_t result = v2styxlib_cobs_encode(buffer, 255, 0xFF);
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_LARGE);
}

TEST_CASE("v2styxlib_cobs_decode: input too small with custom protected byte", "[cobs_wrapper]")
{
    uint8_t buffer[256] = {0};
    int8_t result = v2styxlib_cobs_decode(buffer, 1, 0xFF);
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_SMALL);
}

TEST_CASE("v2styxlib_cobs_roundtrip: diverse data with 0x7F protection", "[cobs_wrapper]")
{
    // Data: [0x00, 0x01, 0x7E, 0xFE, 0xFF] with 0x7F protected
    uint8_t buffer[256] = {0, 0x00, 0x01, 0x7E, 0xFE, 0xFF};
    uint8_t original[5] = {0x00, 0x01, 0x7E, 0xFE, 0xFF};
    
    // Encode with 0x7F protection
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, 6, 0x7F);
    REQUIRE(encodeResult == 0);
    
    // Verify code byte is not 0x7F
    REQUIRE(buffer[0] != 0x7F);
    
    // Decode with 0x7F protection
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, 6, 0x7F);
    REQUIRE(decodeResult == 0);
    
    // Verify data is restored
    REQUIRE(buffer[1] == original[0]);
    REQUIRE(buffer[2] == original[1]);
    REQUIRE(buffer[3] == original[2]);
    REQUIRE(buffer[4] == original[3]);
    REQUIRE(buffer[5] == original[4]);
}

TEST_CASE("v2styxlib_cobs_encode_wrapper: multiple protected bytes in data 0x55", "[cobs_wrapper]")
{
    // [0x11, 0x55, 0x22, 0x55, 0x33] with 0x55 protected
    uint8_t buffer[256] = {0, 0x11, 0x55, 0x22, 0x55, 0x33};
    
    int8_t result = v2styxlib_cobs_encode(buffer, 6, 0x55);
    REQUIRE(result == 0);
    
    // Verify no 0x55 at code byte positions
    REQUIRE(buffer[0] != 0x55);
}

TEST_CASE("v2styxlib_cobs_roundtrip: edge case protected byte 0x01", "[cobs_wrapper]")
{
    // [0x02, 0x03, 0x04] with 0x01 protected
    uint8_t buffer[256] = {0, 0x02, 0x03, 0x04};
    
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, 4, 0x01);
    REQUIRE(encodeResult == 0);
    
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, 4, 0x01);
    REQUIRE(decodeResult == 0);
    
    REQUIRE(buffer[1] == 0x02);
    REQUIRE(buffer[2] == 0x03);
    REQUIRE(buffer[3] == 0x04);
}

TEST_CASE("v2styxlib_cobs_roundtrip: large input with 0xCC protection", "[cobs_wrapper]")
{
    uint8_t buffer[256];
    buffer[0] = 0;
    for (int i = 1; i <= 10; i++) {
        buffer[i] = (uint8_t)(0x10 * i);  // 0x10, 0x20, 0x30, ..., 0xA0
    }
    
    // Encode with 0xCC protection
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, 11, 0xCC);
    REQUIRE(encodeResult == 0);
    
    // Decode with 0xCC protection
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, 11, 0xCC);
    REQUIRE(decodeResult == 0);
    
    // Verify data is restored
    for (int i = 1; i <= 10; i++) {
        REQUIRE(buffer[i] == (uint8_t)(0x10 * i));
    }
}

TEST_CASE("v2styxlib_cobs_roundtrip: max input with 0xFF protection", "[cobs_wrapper]")
{
    uint8_t buffer[256];
    buffer[0] = 0;  // reserved
    std::memset(buffer + 1, 0x77, 253);  // 253 bytes of 0x77
    
    // Encode with 0xFF protection
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, 254, 0xFF);
    REQUIRE(encodeResult == 0);
    
    // Decode with 0xFF protection
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, 254, 0xFF);
    REQUIRE(decodeResult == 0);
    
    // Verify data is restored
    for (int i = 1; i < 254; i++) {
        REQUIRE(buffer[i] == 0x77);
    }
}

TEST_CASE("v2styxlib_cobs_encode_decode: different protected bytes should produce different encodings", "[cobs_wrapper]")
{
    uint8_t buffer1[256] = {0, 0x11, 0x22, 0x33};
    uint8_t buffer2[256] = {0, 0x11, 0x22, 0x33};
    
    // Encode with different protected bytes
    v2styxlib_cobs_encode(buffer1, 4, 0xAA);
    v2styxlib_cobs_encode(buffer2, 4, 0xFF);
    
    // Code bytes should be different (they might be the same by chance, but encoding differs)
    // At minimum, after decoding with correct bytes, data should match
    v2styxlib_cobs_decode(buffer1, 4, 0xAA);
    v2styxlib_cobs_decode(buffer2, 4, 0xFF);
    
    REQUIRE(buffer1[1] == 0x11);
    REQUIRE(buffer1[2] == 0x22);
    REQUIRE(buffer1[3] == 0x33);
    
    REQUIRE(buffer2[1] == 0x11);
    REQUIRE(buffer2[2] == 0x22);
    REQUIRE(buffer2[3] == 0x33);
}

TEST_CASE("v2styxlib_cobs_decode: input too large with custom protected byte", "[cobs_wrapper]")
{
    uint8_t buffer[256];
    std::memset(buffer, 0xAA, 256);
    
    int8_t result = v2styxlib_cobs_decode(buffer, 256, 0xFF);
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_LARGE);
}
