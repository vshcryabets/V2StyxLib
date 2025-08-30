#include <catch2/catch_test_macros.hpp>
#include "messages/v9p2000/MessageFactoryImpl.h"
#include "serialization/StyxSerializerImpl.h"
#include "serialization/BufferWriterImpl.h"
#include "messages/v9p2000/BaseMessage.h"

using namespace styxlib::messages::v9p2000;

class Suite
{
public:
    MessageFactoryImpl messageFactory;
    Suite()
    {
    }
};

using BaseMessage = styxlib::messages::v9p2000::BaseMessage;

TEST_CASE("testCreateTVersion", "[MessageFactoryImpl]")
{
    Suite suite;
    std::unique_ptr<StyxMessage> message = suite.messageFactory.constructTVersion(16384, "9P2000");
    REQUIRE(message != nullptr);
    REQUIRE(((BaseMessage *)message.get())->getIounit() == 16384);
    REQUIRE(((BaseMessage *)message.get())->getProtocolVersion() == "9P2000");
}