#include <catch2/catch_test_macros.hpp>
#include "serialization/BufferWriterImpl.h"

TEST_CASE("testWriteUInt8", "[BufferWriterImpl]")
{
    BufferWriterImpl writer(32);
    writer.writeUInt8(171);
    StyxBuffer buf = writer.getBuffer();
    REQUIRE(171 == buf[0]);
}

TEST_CASE("testWriteUInt16", "[BufferWriterImpl]")
{
    BufferWriterImpl writer(32);
    writer.writeUInt16(0xBEEF);
    StyxBuffer buf = writer.getBuffer();
    REQUIRE(0xEF == buf[0]);
    REQUIRE(0xBE == buf[1]);
}

TEST_CASE("testWriteUInt32", "[BufferWriterImpl]")
{
    BufferWriterImpl writer(32);
    writer.writeUInt32(0xCAFEBABE);
    StyxBuffer buf = writer.getBuffer();
    REQUIRE(0xBE == buf[0]);
    REQUIRE(0xBA == buf[1]);
    REQUIRE(0xFE == buf[2]);
    REQUIRE(0xCA == buf[3]);
}

TEST_CASE("testWriteUInt64", "[BufferWriterImpl]")
{
    BufferWriterImpl writer(32);
    writer.writeUInt64(0x1122334455667788L);
    StyxBuffer buf = writer.getBuffer();
    REQUIRE(0x88 == buf[0]);
    REQUIRE(0x77 == buf[1]);
    REQUIRE(0x66 == buf[2]);
    REQUIRE(0x55 == buf[3]);
    REQUIRE(0x44 == buf[4]);
    REQUIRE(0x33 == buf[5]);
    REQUIRE(0x22 == buf[6]);
    REQUIRE(0x11 == buf[7]);
}

TEST_CASE("testWriteUTFString", "[BufferWriterImpl]")
{
    BufferWriterImpl writer(32);
    StyxString str = "Test";
    writer.writeUTFString(str);
    StyxBuffer buf = writer.getBuffer();
    uint16_t len = (buf[0] & 0xFF) | ((buf[1] & 0xFF) << 8);
    REQUIRE(len == str.length());
    REQUIRE(buf[2] == 'T');
    REQUIRE(buf[3] == 'e');
    REQUIRE(buf[4] == 's');
    REQUIRE(buf[5] == 't');
}

TEST_CASE("testWriteAndPrepareBuffer", "[BufferWriterImpl]")
{
    BufferWriterImpl writer(32);
    writer.writeUInt8(0x01);
    writer.prepareBuffer(16);
    REQUIRE(0 == writer.getPosition());
    REQUIRE(16 == writer.getLimit());
}

TEST_CASE("testWriteBufferOverflow", "[BufferWriterImpl]")
{
    BufferWriterImpl writer(2);
    REQUIRE_THROWS(writer.writeUInt32(0xCAFEBABE));
}