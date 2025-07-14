#include <catch2/catch_test_macros.hpp>
#include "serialization/StyxSerializerImpl.h"
#include "structs/StyxStat.h"
#include "enums/QidType.h"
#include "serialization/BufferWriterImpl.h"

TEST_CASE("testGetSize", "[StyxSerializationImpl]")
{
    StyxSerializerImpl serializer;

    REQUIRE(IDataSerializer::BASE_BINARY_SIZE + 2 + 2 ==
            serializer.getMessageSize(styxlib::messages::StyxRErrorMessage(0, "AB")));
}

TEST_CASE("testGetStyxStatSize", "[StyxSerializationImpl]")
{
    StyxSerializerImpl impl;
    REQUIRE(28 + 13 + 2 + 2 + 2 + 2 == impl.getStatSerializedSize(StyxStat::EMPTY));
}

TEST_CASE("testGetQidSize", "[StyxSerializationImpl]")
{
    StyxSerializerImpl impl;
    REQUIRE(13 == impl.getQidSize());
}

TEST_CASE("testQidSerialization", "[StyxSerializationImpl]")
{
    StyxSerializerImpl serializer;
    StyxQID qid(
        styxlib::enums::QTDIR,
        0x6A7470F1,
        0x12309E51049E5104L);
    uint8_t expected[] = {
        styxlib::enums::QTDIR,
        0xF1, 0x70, 0x74, 0x6A,                        // 9: qid.version[4] 0x6A7470F1
        0x04, 0x51, 0x9E, 0x04, 0x51, 0x9E, 0x30, 0x12 // 13: qid.path[8] 0x12309E51049E5104L
    };
    BufferWriterImpl outputBuffer(8192);
    serializer.serializeQid(qid, outputBuffer);

    REQUIRE(outputBuffer.getPosition() == serializer.getQidSize());
    StyxBuffer buffer = outputBuffer.getBuffer();
    REQUIRE(std::equal(std::begin(expected), std::end(expected), buffer));
}

TEST_CASE("testSerializeStat", "[StyxSerializationImpl]")
{
    StyxSerializerImpl serializer;

    StyxStat stat{
        .type = 1,
        .dev = 2,
        .QID = StyxQID(styxlib::enums::QTFILE, 0x80, 0x90),
        .mode = 0x01,
        .accessTime = 1717171717, // fixed date for reproducibility
        .modificationTime = 1717171717,
        .length = 0x123,
        .name = "file",
        .userName = "user",
        .groupName = "group",
        .modificationUser = "editor"};
    BufferWriterImpl output(8192);
    serializer.serializeStat(stat, output);

    // Validate buffer size and some expected values
    StyxBuffer buffer = output.getBuffer();

    REQUIRE(serializer.getStatSerializedSize(stat) == output.getPosition());

    uint8_t expected[] = {
        66, 0x00,               // size - 2
        1, 0x00,                // type
        0x02, 0x00, 0x00, 0x00, // dev
        styxlib::enums::QTFILE,
        0x80, 0x00, 0x00, 0x00,                         // 9: qid.version[4]
        0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 13: qid.path[8] 0x12309E51049E5104L
        0x01, 0x00, 0x00, 0x00,                         // mode
        0x05, 0xF6, 89, 102,                            // atime
        0x05, 0xF6, 89, 102,                            // mtime
        0x23, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // length
        0x04, 0x00,                                     // name length
        'f', 'i', 'l', 'e',                             // name
        0x04, 0x00,                                     // uid length
        'u', 's', 'e', 'r',                             // uid
        0x05, 0x00,                                     // gid length
        'g', 'r', 'o', 'u', 'p',                        // gid
        0x06, 0x00,                                     // muid length
        'e', 'd', 'i', 't', 'o', 'r'                    // muid
    };
    REQUIRE(std::equal(std::begin(expected), std::end(expected), buffer));
}