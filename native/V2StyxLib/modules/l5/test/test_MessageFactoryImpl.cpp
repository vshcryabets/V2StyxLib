#include <catch2/catch_test_macros.hpp>
#include "messages/v9p2000/MessageFactoryImpl.h"
#include "serialization/StyxSerializerImpl.h"
#include "serialization/BufferWriterImpl.h"

using namespace styxlib::messages::v9p2000;

class Suite
{
public:
    MessageFactoryImpl messageFactory;
    StyxSerializerImpl serializer;
    BufferWriterImpl outputBuffer;
    Suite() : serializer(), outputBuffer(8192)
    {
    }
};

TEST_CASE("testTVersion", "[MessageFactoryImpl]")
{
    Suite suite;
}