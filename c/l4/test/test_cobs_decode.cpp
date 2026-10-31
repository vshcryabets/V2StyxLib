#include <catch2/catch_test_macros.hpp>
#include <vector>
#include <cstring>

extern "C" {
#include "Channel_c.h"
}

TEST_CASE("v2styxlib_cobs_decode_0: null buffer", "[cobs_decode_0]")
{
    int8_t result = v2styxlib_cobs_decode_0(nullptr, 5);
    REQUIRE(result == COBS_ENCODE_NULL_BUFFER);
}

TEST_CASE("v2styxlib_cobs_decode_0: input too small", "[cobs_decode_0]")
{
    uint8_t buffer[256] = {0};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 1);
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_SMALL);
}

TEST_CASE("v2styxlib_cobs_decode_0: input too large", "[cobs_decode_0]")
{
    uint8_t buffer[256];
    std::memset(buffer, 0xAA, 256);
    
    int8_t result = v2styxlib_cobs_decode_0(buffer, 256);
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_LARGE);
}

TEST_CASE("v2styxlib_cobs_decode_0: simple single protected byte", "[cobs_decode_0]")
{
    // Encoded: [1 (code=1, protected at 0+1=1), 0xAA]
    // After decode: buffer[1] becomes 0x00
    uint8_t buffer[256] = {1, 0xAA};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 2);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[1] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode_0: code 2 protects at position 2", "[cobs_decode_0]")
{
    // Encoded: [2 (code=2, protected at 0+2=2), 0x11, 0x22]
    // After decode: buffer[2] becomes 0x00
    uint8_t buffer[256] = {2, 0x11, 0x22};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 3);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode_0: two protected bytes", "[cobs_decode_0]")
{
    // Encoded: [1 (protect at 1), 1 (protect at 1+1=2), 0x33]
    // After decode: buffer[1]=0x00, buffer[2]=0x00
    uint8_t buffer[256] = {1, 1, 0x33};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 3);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[1] == 0x00);
    REQUIRE(buffer[2] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode_0: data then protected", "[cobs_decode_0]")
{
    // Encoded: [3 (protect at 3), 0x11, 0x22, 0x33]
    // After decode: buffer[3] becomes 0x00
    uint8_t buffer[256] = {3, 0x11, 0x22, 0x33};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 4);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 0x22);
    REQUIRE(buffer[3] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode_0: protect, data, protect", "[cobs_decode_0]")
{
    // Encoded: [1 (protect at 1), 2 (protect at 1+2=3), 0xAA, 0xBB]
    // After decode: buffer[1]=0x00, buffer[2]=0xAA, buffer[3]=0x00
    uint8_t buffer[256] = {1, 2, 0xAA, 0xBB};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 4);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[1] == 0x00);
    REQUIRE(buffer[2] == 0xAA);
    REQUIRE(buffer[3] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode_0: skip many bytes", "[cobs_decode_0]")
{
    // Encoded: [10 (protect at 10), followed by 9 data bytes]
    uint8_t buffer[256];
    buffer[0] = 10;
    for (int i = 1; i < 10; i++) {
        buffer[i] = (uint8_t)(0x10 + i);
    }
    
    int8_t result = v2styxlib_cobs_decode_0(buffer, 10);
    
    REQUIRE(result == 0);
    for (int i = 1; i < 10; i++) {
        REQUIRE(buffer[i] == (uint8_t)(0x10 + i));
    }
    REQUIRE(buffer[10] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode_0: code byte 0x00 is error", "[cobs_decode_0]")
{
    // First code cannot be 0 - that would mean protected byte at position 0
    uint8_t buffer[256] = {0, 0x11};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 2);
    
    REQUIRE(result == COBS_ENCODE_PROTECTED_BYTE_IN_DATA);
}

TEST_CASE("v2styxlib_cobs_decode_0: second code byte 0x00 is error", "[cobs_decode_0]")
{
    // Encoded: [3 (protect at 3), 0x11, 0x22, 0 (INVALID - this is code byte with value 0)]
    uint8_t buffer[256] = {3, 0x11, 0x22, 0};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 4);
    
    REQUIRE(result == COBS_ENCODE_PROTECTED_BYTE_IN_DATA);
}

TEST_CASE("v2styxlib_cobs_decode_0: complex multi-segment", "[cobs_decode_0]")
{
    // Encoded: [2 (protect at 2), 0x11, 2 (protect at 2+2=4), 0x22, 1 (protect at 4+1=5)]
    // After decode: positions 2,4,5 are 0x00
    uint8_t buffer[256] = {2, 0x11, 2, 0x22, 1};
    int8_t result = v2styxlib_cobs_decode_0(buffer, 5);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 0x00);
    REQUIRE(buffer[3] == 0x22);
    REQUIRE(buffer[4] == 0x00);
    REQUIRE(buffer[5] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode_0: max skip (254)", "[cobs_decode_0]")
{
    uint8_t buffer[256];
    buffer[0] = 254;
    for (int i = 1; i < 254; i++) {
        buffer[i] = 0xCC;
    }
    
    int8_t result = v2styxlib_cobs_decode_0(buffer, 254);
    
    REQUIRE(result == 0);
    for (int i = 1; i < 254; i++) {
        REQUIRE(buffer[i] == 0xCC);
    }
    REQUIRE(buffer[254] == 0x00);
}

TEST_CASE("v2styxlib_cobs_decode_0: roundtrip - simple no protected bytes", "[cobs_decode_0]")
{
    // Original input: [0x11, 0x22, 0x33] (no 0x00 bytes)
    uint8_t buffer[256] = {0, 0x11, 0x22, 0x33};
    
    // Encode
    int8_t encodeResult = v2styxlib_cobs_encode_0(buffer, 4);
    REQUIRE(encodeResult == 0);
    
    // Should be: [3 (code), 0x11, 0x22, 0x33]
    REQUIRE(buffer[0] == 3);
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 0x22);
    REQUIRE(buffer[3] == 0x33);
    
    // Decode
    int8_t decodeResult = v2styxlib_cobs_decode_0(buffer, 4);
    REQUIRE(decodeResult == 0);
    
    // After decode: [3, 0x11, 0x22, 0x00] - position 4 was written with 0x00
    REQUIRE(buffer[4] == 0x00);
}


