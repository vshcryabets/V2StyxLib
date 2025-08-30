#include <catch2/catch_test_macros.hpp>
#include "messages/v9p2000/MessageFactoryImpl.h"
#include "serialization/StyxSerializerImpl.h"
#include "serialization/BufferWriterImpl.h"
#include "messages/v9p2000/BaseMessage.h"
#include <iostream>

using namespace styxlib::messages::v9p2000;
using namespace styxlib::messages::base;

class Suite
{
public:
    MessageFactoryImpl messageFactory;
    Suite()
    {
    }
};

TEST_CASE("testCreateTVersion", "[MessageFactoryImpl]")
{
    Suite suite;
    StyxMessageUPtr message = suite.messageFactory.constructTVersion(16384, "9P2000");
    REQUIRE(message.get() != nullptr);
    REQUIRE(((BaseMessage *)message.get())->getIounit() == 16384);
    REQUIRE(((BaseMessage *)message.get())->getProtocolVersion() == "9P2000");
}

TEST_CASE("testCreateRerror", "[MessageFactoryImpl]")
{
    Suite suite;
    StyxMessageUPtr message = suite.messageFactory.constructRerror(1, "Test error");
    REQUIRE(message.get() != nullptr);
    REQUIRE(((BaseMessage *)message.get())->getTag() == 1);
    REQUIRE(((StyxRErrorMessage *)message.get())->getMessage() == "Test error");
}
