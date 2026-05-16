#include <catch2/catch_test_macros.hpp>

#include <vector>

#include "uart/ChannelUart.h"

namespace
{

class TestChannelUart final : public styxlib::ChannelUart
{
public:
    explicit TestChannelUart(const styxlib::ChannelUartConfig* config)
        : styxlib::ChannelUart(config)
    {
    }

    std::vector<uint8_t> sentBytes;

protected:
    styxlib::SizeResult internalSendBytes(const uint8_t* buffer, styxlib::Size size) override
    {
        sentBytes.insert(sentBytes.end(), buffer, buffer + size);
        return size;
    }
};

}

TEST_CASE("ChannelUart::sendBuffer frames payload with SOF, header, CRC and data", "[ChannelUart]")
{
    styxlib::ChannelUartConfig config(
        V2STYXLIB_CONFIG_STREAMING_MODE | V2STYXLIB_CONFIG_SEND_CRC16);
    config.packetSizeHeader = styxlib::PacketHeaderSize::Size2Bytes;

    TestChannelUart channel(&config);
    std::vector<uint8_t> payload = { 0x10, 0x20, 0x30 };

    auto result = channel.sendBuffer(
        styxlib::InvalidClientId,
        payload.data(),
        static_cast<styxlib::Size>(payload.size()));

    REQUIRE(result.has_value());
    REQUIRE(result.value() == payload.size());

    const uint16_t crc = v2styxlib_crc16_calculate(
        payload.data(),
        static_cast<BufferSize_t>(payload.size()));
    const std::vector<uint8_t> expected = {
        V2STYXLIB_SOF_MARKER_1,
        V2STYXLIB_SOF_MARKER_2,
        0x00,
        0x05,
        static_cast<uint8_t>((crc >> 8) & 0xFF),
        static_cast<uint8_t>(crc & 0xFF),
        0x10,
        0x20,
        0x30,
    };

    REQUIRE(channel.sentBytes == expected);
}

TEST_CASE("ChannelUart::sendBuffer frames payload with header and data only", "[ChannelUart]")
{
    styxlib::ChannelUartConfig config(0);
    config.packetSizeHeader = styxlib::PacketHeaderSize::Size1Byte;

    TestChannelUart channel(&config);
    std::vector<uint8_t> payload = { 0xAB, 0xCD };

    auto result = channel.sendBuffer(
        styxlib::InvalidClientId,
        payload.data(),
        static_cast<styxlib::Size>(payload.size()));

    REQUIRE(result.has_value());
    REQUIRE(result.value() == payload.size());

    const std::vector<uint8_t> expected = {
        0x02,
        0xAB,
        0xCD,
    };

    REQUIRE(channel.sentBytes == expected);
}