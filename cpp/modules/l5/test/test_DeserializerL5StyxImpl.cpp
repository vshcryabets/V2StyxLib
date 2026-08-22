#include <catch2/catch_test_macros.hpp>

#include <stdexcept>
#include <vector>

#include "enums/MessageType.h"
#include "messages/v9p2000/BaseMessage.h"
#include "serialization/DeserializerL5StyxImpl.h"

using styxlib::messages::base::StyxMessage;
using styxlib::messages::v9p2000::StyxRErrorMessage;

namespace
{
    class TestConsumer : public styxlib::DeserializerL5::Consumer
    {
    public:
        bool called{false};
        styxlib::ClientId lastClientId{0};
        styxlib::Type lastType{0};
        styxlib::Tag lastTag{0};
        styxlib::StyxString lastError;

        void handleMessage(
            styxlib::ClientId clientId,
            const styxlib::messages::base::StyxMessageUPtr &message) override
        {
            called = true;
            lastClientId = clientId;
            lastType = message->getType();
            lastTag = message->getTag();

            if (message->getType() == styxlib::enums::Rerror)
            {
                const auto *errorMessage = static_cast<const StyxRErrorMessage *>(message.get());
                lastError = errorMessage->getMessage();
            }
        }
    };
}

TEST_CASE("DeserializerL5StyxImpl rejects null buffer", "[DeserializerL5StyxImpl]")
{
    styxlib::DeserializerL5StyxImpl deserializer;

    REQUIRE(
        deserializer.handleBuffer(1, nullptr, 1) ==
        styxlib::ErrorCode::NullptrArgument);
}

TEST_CASE("DeserializerL5StyxImpl ignores messages without consumer", "[DeserializerL5StyxImpl]")
{
    styxlib::DeserializerL5StyxImpl deserializer;

    // Packet: size=7, type=Tflush, tag=0x1234, oldTag=0x0001
    uint8_t packet[] = {
        0x09, 0x00, 0x00, 0x00,
        static_cast<uint8_t>(styxlib::enums::Tflush),
        0x34, 0x12,
        0x01, 0x00};

    REQUIRE(
        deserializer.handleBuffer(7, packet, sizeof(packet)) ==
        styxlib::ErrorCode::Success);
}

TEST_CASE("DeserializerL5StyxImpl deserializes Rerror and passes message to consumer", "[DeserializerL5StyxImpl]")
{
    styxlib::DeserializerL5StyxImpl deserializer;
    TestConsumer consumer;
    deserializer.setConsumer(&consumer);

    // Packet: size=12, type=Rerror, tag=0x1234, error=UTF("ERR")
    uint8_t packet[] = {
        0x0C, 0x00, 0x00, 0x00,
        static_cast<uint8_t>(styxlib::enums::Rerror),
        0x34, 0x12,
        0x03, 0x00,
        'E', 'R', 'R'};

    REQUIRE(
        deserializer.handleBuffer(42, packet, sizeof(packet)) ==
        styxlib::ErrorCode::Success);

    REQUIRE(consumer.called);
    REQUIRE(consumer.lastClientId == 42);
    REQUIRE(consumer.lastType == styxlib::enums::Rerror);
    REQUIRE(consumer.lastTag == 0x1234);
    REQUIRE(consumer.lastError == "ERR");
}

TEST_CASE("DeserializerL5StyxImpl validates packet size against ioUnit", "[DeserializerL5StyxImpl]")
{
    styxlib::DeserializerL5StyxImpl deserializer(/*ioUnit*/ 8);
    TestConsumer consumer;
    deserializer.setConsumer(&consumer);

    // Declared packet size 12 exceeds ioUnit 8.
    uint8_t packet[] = {
        0x0C, 0x00, 0x00, 0x00,
        static_cast<uint8_t>(styxlib::enums::Rerror),
        0x34, 0x12,
        0x03, 0x00,
        'E', 'R', 'R'};

    REQUIRE(
        deserializer.handleBuffer(1, packet, sizeof(packet)) ==
        styxlib::ErrorCode::PacketTooLarge);
    REQUIRE_FALSE(consumer.called);
}
