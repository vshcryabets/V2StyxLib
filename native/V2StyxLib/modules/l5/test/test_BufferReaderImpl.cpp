#include <catch2/catch_test_macros.hpp>
#include "serialization/BufferReaderImpl.h"

TEST_CASE("readUTFString", "[BufferReaderImpl]") {
    uint8_t testData[] = {0x05, 0x00, 'A', 'B', 'C', 'D', 'E', 'F', 'G'};
    BufferReaderImpl impl(testData, sizeof(testData));
    REQUIRE("ABCDE" == impl.readUTFString());
}

TEST_CASE("readUInt8", "[BufferReaderImpl]") {
    uint8_t testData[] = {0x05, 0x06};
    BufferReaderImpl impl(testData, sizeof(testData));
    REQUIRE(0x05 == impl.readUInt8());
    REQUIRE(0x06 == impl.readUInt8());
}

TEST_CASE("readUInt16", "[BufferReaderImpl]") {
    uint8_t testData[] = {0x05, 0x06, 0x07, 0x08};
    BufferReaderImpl impl(testData, sizeof(testData));
    REQUIRE(0x0605 == impl.readUInt16());
    REQUIRE(0x0807 == impl.readUInt16());
}

TEST_CASE("readUInt32", "[BufferReaderImpl]") {
    uint8_t testData[] = {0x05, 0x06, 0x07, 0x08, 0x10, 0x11, 0x12, 0x13};
    BufferReaderImpl impl(testData, sizeof(testData));
    REQUIRE(0x08070605 == impl.readUInt32());
    REQUIRE(0x13121110 == impl.readUInt32());
}

TEST_CASE("readUInt64", "[BufferReaderImpl]") {
    uint8_t testData[] = {0x05, 0x06, 0x07, 0x08, 0x10, 0x11, 0x12, 0x13,
                0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27};
    BufferReaderImpl impl(testData, sizeof(testData));
    REQUIRE(0x1312111008070605ULL == impl.readUInt64());
    REQUIRE(0x2726252423222120ULL == impl.readUInt64());
}
