#include <catch2/catch_test_macros.hpp>
#include <vector>
#include <cstring>

extern "C" {
#include "Channel_c.h"
}

TEST_CASE("v2styxlib_cobs_encode: null buffer", "[cobs_encode]")
{
    int8_t result = v2styxlib_cobs_encode(nullptr, 5, 0x00);
    
    REQUIRE(result == COBS_ENCODE_NULL_BUFFER);
}

TEST_CASE("v2styxlib_cobs_encode: input too small (less than 2)", "[cobs_encode]")
{
    uint8_t buffer[256] = {0};
    int8_t result = v2styxlib_cobs_encode(buffer, 1, 0x00);
    
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_SMALL);
}

TEST_CASE("v2styxlib_cobs_encode: input too large (255 bytes)", "[cobs_encode]")
{
    uint8_t buffer[256];
    std::memset(buffer, 0xAA, 255);
    
    int8_t result = v2styxlib_cobs_encode(buffer, 255, 0x00);
    
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_LARGE);
}

TEST_CASE("v2styxlib_cobs_encode: two bytes no protected byte", "[cobs_encode]")
{
    // buffer[0] is reserved, input data at buffer[1]
    uint8_t buffer[256] = {0, 0xAA};
    int8_t result = v2styxlib_cobs_encode(buffer, 2, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 2);    // code byte: distance to next protected (or end) = 2 (code + 1 data)
    REQUIRE(buffer[1] == 0xAA); // original data
}

TEST_CASE("v2styxlib_cobs_encode: three bytes no protected byte", "[cobs_encode]")
{
    // buffer[0] reserved, data at buffer[1..2]
    uint8_t buffer[256] = {0, 0x11, 0x22};
    int8_t result = v2styxlib_cobs_encode(buffer, 3, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 3);    // code byte: 3 (code + 2 data bytes)
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 0x22);
}

TEST_CASE("v2styxlib_cobs_encode: protected byte in middle", "[cobs_encode]")
{
    // buffer[0] reserved, data: 0x11, 0x00, 0x22
    uint8_t buffer[256] = {0, 0x11, 0x00, 0x22};
    int8_t result = v2styxlib_cobs_encode(buffer, 4, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 2);    // code: 2 (code + 1 data byte before protected)
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 2);    // code: 2 (code + 1 data byte after protected)
    REQUIRE(buffer[3] == 0x22);
}

TEST_CASE("v2styxlib_cobs_encode: multiple protected bytes", "[cobs_encode]")
{
    // buffer[0] reserved, data: 0x11, 0x00, 0x22, 0x00, 0x33
    uint8_t buffer[256] = {0, 0x11, 0x00, 0x22, 0x00, 0x33};
    int8_t result = v2styxlib_cobs_encode(buffer, 6, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 2);    // code: 0x11 before first protected
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 2);    // code: 0x22 between protected bytes
    REQUIRE(buffer[3] == 0x22);
    REQUIRE(buffer[4] == 2);    // code: 0x33 after second protected
    REQUIRE(buffer[5] == 0x33);
}

TEST_CASE("v2styxlib_cobs_encode: protected byte at start of data", "[cobs_encode]")
{
    // buffer[0] reserved, data: 0x00, 0x11, 0x22
    uint8_t buffer[256] = {0, 0x00, 0x11, 0x22};
    int8_t result = v2styxlib_cobs_encode(buffer, 4, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 1);    // code: no data before first protected byte
    REQUIRE(buffer[1] == 3);    // code: 2 data bytes after first protected
    REQUIRE(buffer[2] == 0x11);
    REQUIRE(buffer[3] == 0x22);
}

TEST_CASE("v2styxlib_cobs_encode: protected byte at end of data", "[cobs_encode]")
{
    // buffer[0] reserved, data: 0x11, 0x22, 0x00
    uint8_t buffer[256] = {0, 0x11, 0x22, 0x00};
    int8_t result = v2styxlib_cobs_encode(buffer, 4, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 3);    // code: 2 data bytes before protected
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 0x22);
    REQUIRE(buffer[3] == 1);    // final code: 1 (no data after protected)
}

TEST_CASE("v2styxlib_cobs_encode: consecutive protected bytes", "[cobs_encode]")
{
    // buffer[0] reserved, data: 0x00, 0x00, 0x11
    uint8_t buffer[256] = {0, 0x00, 0x00, 0x11};
    int8_t result = v2styxlib_cobs_encode(buffer, 4, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 1);    // code: nothing before first protected
    REQUIRE(buffer[1] == 1);    // code: nothing between protected bytes
    REQUIRE(buffer[2] == 2);    // code: 1 data byte after second protected
    REQUIRE(buffer[3] == 0x11);
}

TEST_CASE("v2styxlib_cobs_encode: different protected byte (0xFF)", "[cobs_encode]")
{
    // buffer[0] reserved, data: 0x11, 0xFF, 0x22
    uint8_t buffer[256] = {0, 0x11, 0xFF, 0x22};
    int8_t result = v2styxlib_cobs_encode(buffer, 4, 0xFF);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 2);    // code: 1 data byte before protected 0xFF
    REQUIRE(buffer[1] == 0x11);
    REQUIRE(buffer[2] == 2);    // code: 1 data byte after protected
    REQUIRE(buffer[3] == 0x22);
}

TEST_CASE("v2styxlib_cobs_encode: max 254 byte input", "[cobs_encode]")
{
    uint8_t buffer[256];
    buffer[0] = 0;  // reserved
    std::memset(buffer + 1, 0xBB, 253);  // 253 bytes of data
    
    int8_t result = v2styxlib_cobs_encode(buffer, 254, 0x00);
    
    // 253 non-protected bytes should encode as: [254 (code)] + [253 bytes of 0xBB]
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 254);
    for (int i = 1; i < 254; i++) {
        REQUIRE(buffer[i] == 0xBB);
    }
}

TEST_CASE("v2styxlib_cobs_encode: 255 byte input should fail", "[cobs_encode]")
{
    uint8_t buffer[256];
    std::memset(buffer, 0xCC, 255);
    
    int8_t result = v2styxlib_cobs_encode(buffer, 255, 0x00);
    
    REQUIRE(result == COBS_ENCODE_INPUT_TOO_LARGE);
}

TEST_CASE("v2styxlib_cobs_encode: buffer with mixed zeros and data", "[cobs_encode]")
{
    // buffer[0] reserved, data: 0x01, 0x02, 0x00, 0x03, 0x04, 0x00, 0x05
    uint8_t buffer[256] = {0, 0x01, 0x02, 0x00, 0x03, 0x04, 0x00, 0x05};
    int8_t result = v2styxlib_cobs_encode(buffer, 8, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 3);    // code: [0x01][0x02]
    REQUIRE(buffer[1] == 0x01);
    REQUIRE(buffer[2] == 0x02);
    REQUIRE(buffer[3] == 3);    // code: [0x03][0x04]
    REQUIRE(buffer[4] == 0x03);
    REQUIRE(buffer[5] == 0x04);
    REQUIRE(buffer[6] == 2);    // code: [0x05]
    REQUIRE(buffer[7] == 0x05);
}

TEST_CASE("v2styxlib_cobs_encode: all data bytes same value", "[cobs_encode]")
{
    uint8_t buffer[256];
    buffer[0] = 0;  // reserved
    std::memset(buffer + 1, 0x42, 10);
    
    int8_t result = v2styxlib_cobs_encode(buffer, 11, 0x00);
    
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 11);   // code: 10 data bytes
    for (int i = 1; i <= 10; i++) {
        REQUIRE(buffer[i] == 0x42);
    }
}

TEST_CASE("v2styxlib_cobs_encode: run of 254 bytes needs split", "[cobs_encode]")
{
    uint8_t buffer[256];
    buffer[0] = 0;  // reserved
    std::memset(buffer + 1, 0xDD, 253);  // 253 bytes
    
    int8_t result = v2styxlib_cobs_encode(buffer, 254, 0x00);
    
    // Should encode as: [254] + [253 bytes of 0xDD]
    REQUIRE(result == 0);
    REQUIRE(buffer[0] == 254);
}
