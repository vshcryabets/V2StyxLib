#include <catch2/catch_test_macros.hpp>
#include <vector>
#include <cstring>

extern "C" {
#include "Channel_c.h"
}

TEST_CASE("v2styxlib_cobs_encode: null buffer", "[cobs_encode]")
{
    int8_t result = v2styxlib_cobs_encode_0(nullptr, 5);
    
    REQUIRE(result == COBS_ENCODE_NULL_BUFFER);
}

TEST_CASE("v2styxlib_cobs_encode: input too small (less than 2)", "[cobs_encode]")
{
    uint8_t buffer[256] = {0};
    int8_t result = v2styxlib_cobs_encode_0(buffer, 1);
    
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_SMALL);
}

TEST_CASE("v2styxlib_cobs_encode: input too large (255 bytes)", "[cobs_encode]")
{
    uint8_t buffer[256];
    std::memset(buffer, 0xAA, 255);
    
    int8_t result = v2styxlib_cobs_encode_0(buffer, 255);
    
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_LARGE);
}

TEST_CASE("v2styxlib_cobs_encode: protected bytes are replaced with offsets", "[cobs_encode]")
{
    // Input data bytes: [0x00, 0x11, 0x00, 0x22, 0x00]
    // Encoded offsets:   [1,    2,    2,    2,    1]
    uint8_t buffer[256] = {0, 0x00, 0x11, 0x00, 0x22, 0x00};
    int8_t result = v2styxlib_cobs_encode_0(buffer, 6);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 1);    // offset to first protected byte at index 1
    REQUIRE(buffer[1] == 2);    // next protected byte at index 3
    REQUIRE(buffer[2] == 0x11); // data unchanged
    REQUIRE(buffer[3] == 2);    // next protected byte at index 5
    REQUIRE(buffer[4] == 0x22); // data unchanged
    REQUIRE(buffer[5] == 1);    // final protected byte points to end
}

TEST_CASE("v2styxlib_cobs_encode: one data byte without protected byte", "[cobs_encode]")
{
    uint8_t buffer[256] = {0, 0xAA};
    int8_t result = v2styxlib_cobs_encode_0(buffer, 2);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 0x02);
    REQUIRE(buffer[1] == 0xAA);
}

TEST_CASE("v2styxlib_cobs_encode: three bytes no protected byte", "[cobs_encode]")
{
    // buffer[0] is reserved, input data at buffer[1]
    uint8_t buffer[] = {0, 0x11, 0x22, 0x33};
    
    int8_t result = v2styxlib_cobs_encode_0(buffer, sizeof(buffer));
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 4);    // code: 3+1 data bytes
    REQUIRE(buffer[1] == 0x11); // original data
    REQUIRE(buffer[2] == 0x22);
    REQUIRE(buffer[3] == 0x33);
}

TEST_CASE("v2styxlib_cobs_encode: 4 bytes no protected byte", "[cobs_encode]")
{
    uint8_t buffer[] = {0, 0x01, 0x02, 0x03, 0x04};
    int8_t result = v2styxlib_cobs_encode_0(buffer, sizeof(buffer));
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 5);    // code: 4 data bytes
    REQUIRE(buffer[1] == 0x01);
    REQUIRE(buffer[2] == 0x02);
    REQUIRE(buffer[3] == 0x03);
    REQUIRE(buffer[4] == 0x04);
}

TEST_CASE("v2styxlib_cobs_encode: max 254 byte input", "[cobs_encode]")
{
    uint8_t buffer[256];
    buffer[0] = 0;  // reserved
    std::memset(buffer + 1, 0xBB, 253);  // 253 bytes of data (no 0x00)
    
    int8_t result = v2styxlib_cobs_encode_0(buffer, 254);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 254);  // first offset points to end
    for (int i = 1; i < 254; i++) {
        REQUIRE(buffer[i] == 0xBB);
    }
}

TEST_CASE("v2styxlib_cobs_encode: 255 byte input should fail", "[cobs_encode]")
{
    uint8_t buffer[256];
    std::memset(buffer, 0xCC, 255);
    
    int8_t result = v2styxlib_cobs_encode_0(buffer, 255);
    
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_LARGE);
}

TEST_CASE("v2styxlib_cobs_encode: code split at 254 bytes", "[cobs_encode]")
{
    uint8_t buffer[256];
    buffer[0] = 0;  // reserved
    std::memset(buffer + 1, 0x55, 253);  // 253 bytes of 0x55
    
    int8_t result = v2styxlib_cobs_encode_0(buffer, 254);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 254);
    for (int i = 1; i < 254; i++) {
        REQUIRE(buffer[i] == 0x55);
    }
}

TEST_CASE("v2styxlib_cobs_encode: data with alternating patterns", "[cobs_encode]")
{
    uint8_t buffer[256];
    buffer[0] = 0;  // reserved
    buffer[1] = 0x55;
    buffer[2] = 0xAA;
    buffer[3] = 0x55;
    buffer[4] = 0xAA;
    
    int8_t result = v2styxlib_cobs_encode_0(buffer, 5);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 5);
    REQUIRE(buffer[1] == 0x55);
    REQUIRE(buffer[2] == 0xAA);
    REQUIRE(buffer[3] == 0x55);
    REQUIRE(buffer[4] == 0xAA);
}

TEST_CASE("v2styxlib_cobs_encode: all bytes same non-zero value", "[cobs_encode]")
{
    uint8_t buffer[256];
    buffer[0] = 0;  // reserved
    std::memset(buffer + 1, 0x42, 10);
    
    int8_t result = v2styxlib_cobs_encode_0(buffer, 11);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 11);   // code: 10 data bytes
    for (int i = 1; i <= 10; i++) {
        REQUIRE(buffer[i] == 0x42);
    }
}
