#include <catch2/catch_test_macros.hpp>
#include "messages/v9p2000/MessageFactoryImpl.h"
#include "serialization/StyxSerializerImpl.h"
#include "serialization/BufferWriterImpl.h"
#include "messages/v9p2000/BaseMessage.h"
#include "messages/v9p2000/StyxTAuthMessage.h"
#include "messages/v9p2000/StyxTAttachMessage.h"

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

TEST_CASE("testCreateTAuth", "[MessageFactoryImpl]")
{
    Suite suite;
    StyxMessageUPtr message = suite.messageFactory.constructTAuth(0x20, "user", "test");
    REQUIRE(message.get() != nullptr);
    REQUIRE(((BaseMessage *)message.get())->getFID() == 0x20);
    REQUIRE(((StyxTAuthMessage *)message.get())->userName == "user");
    REQUIRE(((StyxTAuthMessage *)message.get())->mountPoint == "test");
}

TEST_CASE("testCreateTAttach", "[MessageFactoryImpl]")
{
    Suite suite;
    StyxMessageUPtr message = suite.messageFactory.constructTAttach(1, 2, "user", "test");
    REQUIRE(message.get() != nullptr);
    REQUIRE(((BaseMessage *)message.get())->getFID() == 1);
    REQUIRE(((StyxTAttachMessage *)message.get())->authFID == 2);
    REQUIRE(((StyxTAttachMessage *)message.get())->userName == "user");
    REQUIRE(((StyxTAttachMessage *)message.get())->mountPoint == "test");
}

TEST_CASE("testCreateRerror", "[MessageFactoryImpl]")
{
    Suite suite;
    StyxMessageUPtr message = suite.messageFactory.constructRerror(1, "Test error");
    REQUIRE(message.get() != nullptr);
    REQUIRE(((BaseMessage *)message.get())->getTag() == 1);
    REQUIRE(((StyxRErrorMessage *)message.get())->getMessage() == "Test error");
}

TEST_CASE("testCreateRVersion", "[MessageFactoryImpl]")
{
    Suite suite;
    StyxMessageUPtr message = suite.messageFactory.constructRVersion(1, 16384, "9P2000");
    REQUIRE(message.get() != nullptr);
    REQUIRE(((BaseMessage *)message.get())->getTag() == 1);
    REQUIRE(((BaseMessage *)message.get())->getIounit() == 16384);
    REQUIRE(((BaseMessage *)message.get())->getProtocolVersion() == "9P2000");
}

TEST_CASE("testCreateRAttachMessage", "[MessageFactoryImpl]")
{
    Suite suite;
    StyxMessageUPtr message = suite.messageFactory.constructRAttachMessage(1, QID::EMPTY);
    REQUIRE(message.get() != nullptr);
    REQUIRE(((BaseMessage *)message.get())->getTag() == 1);
    REQUIRE(((BaseMessage *)message.get())->getQID() == QID::EMPTY);
}