#include <catch2/catch_test_macros.hpp>
#include <vector>
#include <cstring>

extern "C" {
#include "Channel_c.h"
}

TEST_CASE("v2styxlib_cobs_decode: null buffer", "[cobs_decode]")
{
    int8_t result = v2styxlib_cobs_decode(nullptr, 5, 0x00);
    
    REQUIRE(result == COBS_ENCODE_NULL_BUFFER);
}

TEST_CASE("v2styxlib_cobs_decode: input too small (less than 2)", "[cobs_decode]")
{
    uint8_t buffer[256] = {0};
    int8_t result = v2styxlib_cobs_decode(buffer, 1, 0x00);
    
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_SMALL);
}

TEST_CASE("v2styxlib_cobs_decode: input too large (256 bytes)", "[cobs_decode]")
{
    uint8_t buffer[256];
    std::memset(buffer, 0xAA, 256);
    
    int8_t result = v2styxlib_cobs_decode(buffer, 256, 0x00);
    
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_LARGE);
}

TEST_CASE("v2styxlib_cobs_decode: two bytes no protected byte", "[cobs_decode]")
{
    // Encoded: [2 (code), 0xAA] -> should decode to [0xAA]
    uint8_t buffer[256] = {2, 0xAA};
    int8_t result = v2styxlib_cobs_decode(buffer, 2, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0xAA);
}

TEST_CASE("v2styxlib_cobs_decode: three bytes no protected byte", "[cobs_decode]")
{
    // Encoded: [3 (code), 0x11, 0x22] -> should decode to [0x11, 0x22]
    uint8_t buffer[256] = {3, 0x11, 0x22};
    int8_t result = v2styxlib_cobs_decode(buffer, 3, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x11);
    REQUIRE(buffer[1] == 0x22);
}

TEST_CASE("v2styxlib_cobs_decode: protected byte in middle", "[cobs_decode]")
{
    // Encoded: [2 (code), 0x11, 2 (code), 0x22]
    // Decoded: [0x11, 0x00 (protected), 0x22]
    uint8_t buffer[256] = {2, 0x11, 2, 0x22};
    int8_t result = v2styxlib_cobs_decode(buffer, 4, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x11);
    REQUIRE(buffer[1] == 0x00);
    REQUIRE(buffer[2] == 0x22);
}

TEST_CASE("v2styxlib_cobs_decode: multiple protected bytes", "[cobs_decode]")
{
    // Encoded: [2 (code), 0x11, 2 (code), 0x22, 2 (code), 0x33]
    // Decoded: [0x11, 0x00, 0x22, 0x00, 0x33]
    uint8_t buffer[256] = {2, 0x11, 2, 0x22, 2, 0x33};
    int8_t result = v2styxlib_cobs_decode(buffer, 6, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x11);
    REQUIRE(buffer[1] == 0x00);
    REQUIRE(buffer[2] == 0x22);
    REQUIRE(buffer[3] == 0x00);
    REQUIRE(buffer[4] == 0x33);
}

TEST_CASE("v2styxlib_cobs_decode: protected byte at start", "[cobs_decode]")
{
    // Encoded: [1 (code), 3 (code), 0x11, 0x22]
    // Decoded: [0x00, 0x11, 0x22]
    uint8_t buffer[256] = {1, 3, 0x11, 0x22};
    int8_t result = v2styxlib_cobs_decode(buffer, 4, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x00);
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 0x22);
}

TEST_CASE("v2styxlib_cobs_decode: protected byte at end", "[cobs_decode]")
{
    // Encoded: [3 (code), 0x11, 0x22, 1 (code)]
    // Decoded: [0x11, 0x22, 0x00]
    uint8_t buffer[256] = {3, 0x11, 0x22, 1};
    int8_t result = v2styxlib_cobs_decode(buffer, 4, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x11);
    REQUIRE(buffer[1] == 0x22);
    REQUIRE(buffer[2] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode: consecutive protected bytes", "[cobs_decode]")
{
    // Encoded: [1 (code), 1 (code), 2 (code), 0x11]
    // Decoded: [0x00, 0x00, 0x11]
    uint8_t buffer[256] = {1, 1, 2, 0x11};
    int8_t result = v2styxlib_cobs_decode(buffer, 4, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x00);
    REQUIRE(buffer[1] == 0x00);
    REQUIRE(buffer[2] == 0x11);
}

TEST_CASE("v2styxlib_cobs_decode: different protected byte (0xFF)", "[cobs_decode]")
{
    // Encoded: [2 (code), 0x11, 2 (code), 0x22]
    // Decoded: [0x11, 0xFF, 0x22]
    uint8_t buffer[256] = {2, 0x11, 2, 0x22};
    int8_t result = v2styxlib_cobs_decode(buffer, 4, 0xFF);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x11);
    REQUIRE(buffer[1] == 0xFF);
    REQUIRE(buffer[2] == 0x22);
}

TEST_CASE("v2styxlib_cobs_decode: mixed data with protected bytes", "[cobs_decode]")
{
    // Encoded: [3 (code), 0x01, 0x02, 3 (code), 0x03, 0x04, 2 (code), 0x05]
    // Decoded: [0x01, 0x02, 0x00, 0x03, 0x04, 0x00, 0x05]
    uint8_t buffer[256] = {3, 0x01, 0x02, 3, 0x03, 0x04, 2, 0x05};
    int8_t result = v2styxlib_cobs_decode(buffer, 8, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x01);
    REQUIRE(buffer[1] == 0x02);
    REQUIRE(buffer[2] == 0x00);
    REQUIRE(buffer[3] == 0x03);
    REQUIRE(buffer[4] == 0x04);
    REQUIRE(buffer[5] == 0x00);
    REQUIRE(buffer[6] == 0x05);
}

TEST_CASE("v2styxlib_cobs_decode: all same data bytes", "[cobs_decode]")
{
    // Encoded: [11 (code), 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42]
    // Decoded: [0x42 x 10]
    uint8_t buffer[256];
    buffer[0] = 11;
    std::memset(buffer + 1, 0x42, 10);
    
    int8_t result = v2styxlib_cobs_decode(buffer, 11, 0x00);
    
    REQUIRE(result == 0);
    for (int i = 0; i < 10; i++) {
        REQUIRE(buffer[i] == 0x42);
    }
}

TEST_CASE("v2styxlib_cobs_decode: roundtrip encode-decode no protected bytes", "[cobs_decode]")
{
    // Original: [0x11, 0x22, 0x33]
    uint8_t buffer[256] = {0, 0x11, 0x22, 0x33};
    uint8_t original[3] = {0x11, 0x22, 0x33};
    
    // Encode
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, 4, 0x00);
    REQUIRE(encodeResult == 0);
    uint8_t encodedLen = 4;  // Code byte + 3 data bytes
    
    // Decode
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, encodedLen, 0x00);
    REQUIRE(decodeResult == 0);
    
    // Should get back original data
    REQUIRE(buffer[0] == original[0]);
    REQUIRE(buffer[1] == original[1]);
    REQUIRE(buffer[2] == original[2]);
}

TEST_CASE("v2styxlib_cobs_decode: roundtrip encode-decode with protected bytes", "[cobs_decode]")
{
    // Original: [0x11, 0x00, 0x22, 0x00, 0x33]
    uint8_t buffer[256] = {0, 0x11, 0x00, 0x22, 0x00, 0x33};
    uint8_t original[5] = {0x11, 0x00, 0x22, 0x00, 0x33};
    
    // Encode
    int8_t encodeResult = v2styxlib_cobs_encode(buffer, 6, 0x00);
    REQUIRE(encodeResult == 0);
    uint8_t encodedLen = 6;  // Code + data + code + data + code + data
    
    // Decode
    int8_t decodeResult = v2styxlib_cobs_decode(buffer, encodedLen, 0x00);
    REQUIRE(decodeResult == 0);
    
    // Should get back original data
    REQUIRE(buffer[0] == original[0]);
    REQUIRE(buffer[1] == original[1]);
    REQUIRE(buffer[2] == original[2]);
    REQUIRE(buffer[3] == original[3]);
    REQUIRE(buffer[4] == original[4]);
}

TEST_CASE("v2styxlib_cobs_decode: max encoded length (255 bytes)", "[cobs_decode]")
{
    // Create a buffer with max encoded data: [254 (code), 253 x 0xBB]
    uint8_t buffer[256];
    buffer[0] = 254;
    std::memset(buffer + 1, 0xBB, 253);
    
    int8_t result = v2styxlib_cobs_decode(buffer, 254, 0x00);
    
    REQUIRE(result == 0);
    for (int i = 0; i < 253; i++) {
        REQUIRE(buffer[i] == 0xBB);
    }
}

TEST_CASE("v2styxlib_cobs_decode: error when protected byte in data", "[cobs_decode]")
{
    // Encoded data should never contain the protected byte
    // This is malformed: [3 (code), 0x11, 0x00, 0x22]
    // The 0x00 shouldn't appear in data
    uint8_t buffer[256] = {3, 0x11, 0x00, 0x22};
    int8_t result = v2styxlib_cobs_decode(buffer, 4, 0x00);
    
    REQUIRE(result == COBS_ENCODE_OUTPUT_TOO_LARGE);
}
