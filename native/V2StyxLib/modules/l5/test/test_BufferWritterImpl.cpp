#include <catch2/catch_test_macros.hpp>
#include "serialization/BufferWritterImpl.h"

TEST_CASE("testWriteUInt8", "[BufferWritterImpl]") {
    BufferWritterImpl writer(32);
    writer.writeUInt8(171);
    uint8_t* buf = writer.getBuffer();
    REQUIRE( 171 == buf[0]);
}

TEST_CASE("testWriteUInt16", "[BufferWritterImpl]") {
    BufferWritterImpl writer(32);
    writer.writeUInt16(0xBEEF);
    uint8_t* buf = writer.getBuffer();
    REQUIRE(0xEF == buf[0]);
    REQUIRE(0xBE == buf[1]);
}

TEST_CASE("testWriteUInt32", "[BufferWritterImpl]") {
    BufferWritterImpl writer(32);
    writer.writeUInt32(0xCAFEBABE);
    uint8_t* buf = writer.getBuffer();
    REQUIRE(0xBE == buf[0]);
    REQUIRE(0xBA == buf[1]);
    REQUIRE(0xFE == buf[2]);
    REQUIRE(0xCA == buf[3]);
}

TEST_CASE("testWriteUInt64", "[BufferWritterImpl]") {
    BufferWritterImpl writer(32);
    writer.writeUInt64(0x1122334455667788L);
    uint8_t* buf = writer.getBuffer();
    REQUIRE(0x88 == buf[0]);
    REQUIRE(0x77 == buf[1]);
    REQUIRE(0x66 == buf[2]);
    REQUIRE(0x55 == buf[3]);
    REQUIRE(0x44 == buf[4]);
    REQUIRE(0x33 == buf[5]);
    REQUIRE(0x22 == buf[6]);
    REQUIRE(0x11 == buf[7]);
}