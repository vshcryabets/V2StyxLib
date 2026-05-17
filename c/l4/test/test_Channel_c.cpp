#include <catch2/catch_test_macros.hpp>
#include <vector>

extern "C" {
#include "Channel_c.h"
}

static std::vector<uint8_t> g_uartBytes;

extern "C" void v2styxlib_uart_send_byte(
    const V2styxlibUartConfig* config,
    uint8_t byte
) {
    (void)config;
    g_uartBytes.push_back(byte);
}

TEST_CASE("v2styxlib_crc16_calculate: empty buffer", "[crc16]")
{
    uint8_t buffer[] = {};
    uint16_t crc = v2styxlib_crc16_calculate(buffer, 0);
    
    // CRC of empty buffer should be the initial value since no data updates it
    REQUIRE(crc == V2STYXLIB_CRC16_INITIAL_VALUE);
}

TEST_CASE("v2styxlib_crc16_calculate: single byte", "[crc16]")
{
    uint8_t buffer[] = { 0x42 };
    uint16_t crc = v2styxlib_crc16_calculate(buffer, sizeof(buffer));
    
    // Verify CRC is calculated (non-zero and deterministic)
    REQUIRE(crc != 0);
    REQUIRE(crc != V2STYXLIB_CRC16_INITIAL_VALUE);
}

TEST_CASE("v2styxlib_crc16_calculate: multiple bytes deterministic", "[crc16]")
{
    uint8_t buffer[] = { 0x01, 0x02, 0x03, 0x04 };
    uint16_t crc1 = v2styxlib_crc16_calculate(buffer, sizeof(buffer));
    uint16_t crc2 = v2styxlib_crc16_calculate(buffer, sizeof(buffer));
    
    // CRC calculation should be deterministic
    REQUIRE(crc1 == crc2);
}

TEST_CASE("v2styxlib_crc16_calculate: different buffers different CRC", "[crc16]")
{
    uint8_t buffer1[] = { 0x01, 0x02, 0x03 };
    uint8_t buffer2[] = { 0x01, 0x02, 0x04 };
    
    uint16_t crc1 = v2styxlib_crc16_calculate(buffer1, sizeof(buffer1));
    uint16_t crc2 = v2styxlib_crc16_calculate(buffer2, sizeof(buffer2));
    
    // Different buffers should produce different CRCs
    REQUIRE(crc1 != crc2);
}

TEST_CASE("v2styxlib_crc16_calculate: message", "[crc16]")
{
    const uint8_t message[] = "Hello, World!";
    uint16_t crc = v2styxlib_crc16_calculate(
        message,
        sizeof(message) - 1); // exclude null terminator
    
    REQUIRE(crc != 0);
    REQUIRE(crc != V2STYXLIB_CRC16_INITIAL_VALUE);
}

TEST_CASE("v2styxlib_uart_configure_proto: set streaming mode", "[uart_config]")
{
    V2styxlibUartConfig config = {0};
    
    v2styxlib_uart_configure_proto(&config, true, false, false);
    
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) != 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) == 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SOFT_UART_TX) == 0);
}

TEST_CASE("v2styxlib_uart_configure_proto: set send CRC16", "[uart_config]")
{
    V2styxlibUartConfig config = {0};
    
    v2styxlib_uart_configure_proto(&config, false, true, false);
    
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) == 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) != 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SOFT_UART_TX) == 0);
}

TEST_CASE("v2styxlib_uart_configure_proto: set soft UART TX", "[uart_config]")
{
    V2styxlibUartConfig config = {0};
    
    v2styxlib_uart_configure_proto(&config, false, false, true);
    
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) == 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) == 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SOFT_UART_TX) != 0);
}

TEST_CASE("v2styxlib_uart_configure_proto: set all flags", "[uart_config]")
{
    V2styxlibUartConfig config = {0};
    
    v2styxlib_uart_configure_proto(&config, true, true, true);
    
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) != 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) != 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SOFT_UART_TX) != 0);
}

TEST_CASE("v2styxlib_uart_configure_proto: clear flags", "[uart_config]")
{
    V2styxlibUartConfig config = {0xFF};
    
    v2styxlib_uart_configure_proto(&config, false, false, false);
    
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) == 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) == 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SOFT_UART_TX) == 0);
}

TEST_CASE("v2styxlib_uart_configure_proto: toggle flags", "[uart_config]")
{
    V2styxlibUartConfig config = {0};
    
    // Set all flags
    v2styxlib_uart_configure_proto(&config, true, true, true);
    REQUIRE(config.baseConfig.config == (V2STYXLIB_CONFIG_STREAMING_MODE | V2STYXLIB_CONFIG_SEND_CRC16 | V2STYXLIB_CONFIG_SOFT_UART_TX));
    
    // Clear streaming mode, keep others
    v2styxlib_uart_configure_proto(&config, false, true, true);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_STREAMING_MODE) == 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SEND_CRC16) != 0);
    REQUIRE((config.baseConfig.config & V2STYXLIB_CONFIG_SOFT_UART_TX) != 0);
}

TEST_CASE("v2styxlib_uart_send: plain frame", "[uart_send]")
{
    V2styxlibUartConfig config = {0};
    v2styxlib_uart_configure_proto(&config, false, false, false);
    uint8_t payload[] = {0x10, 0x20, 0x30};

    g_uartBytes.clear();
    v2styxlib_uart_send(&config, payload, static_cast<BufferSize_t>(sizeof(payload)));

    const std::vector<uint8_t> expected = {0x03, 0x10, 0x20, 0x30};
    REQUIRE(g_uartBytes == expected);
}

TEST_CASE("v2styxlib_uart_send: streaming frame", "[uart_send]")
{
    V2styxlibUartConfig config = {0};
    v2styxlib_uart_configure_proto(&config, true, false, false);
    uint8_t payload[] = {0xAB, 0xCD};

    g_uartBytes.clear();
    v2styxlib_uart_send(&config, payload, static_cast<BufferSize_t>(sizeof(payload)));

    const std::vector<uint8_t> expected = {
        V2STYXLIB_SOF_MARKER_1,
        V2STYXLIB_SOF_MARKER_2,
        0x02,
        0xAB,
        0xCD
    };
    REQUIRE(g_uartBytes == expected);
}

TEST_CASE("v2styxlib_uart_send: crc16 frame", "[uart_send]")
{
    V2styxlibUartConfig config = {0};
    v2styxlib_uart_configure_proto(&config, false, true, false);
    uint8_t payload[] = {0x01, 0x02, 0x03};
    const uint16_t crc = v2styxlib_crc16_calculate(
        payload,
        static_cast<BufferSize_t>(sizeof(payload)));

    g_uartBytes.clear();
    v2styxlib_uart_send(&config, payload, static_cast<BufferSize_t>(sizeof(payload)));

    const std::vector<uint8_t> expected = {
        0x05,
        static_cast<uint8_t>((crc >> 8) & 0xFF),
        static_cast<uint8_t>(crc & 0xFF),
        0x01,
        0x02,
        0x03
    };
    REQUIRE(g_uartBytes == expected);
}

TEST_CASE("v2styxlib_uart_send: streaming + crc16 frame", "[uart_send]")
{
    V2styxlibUartConfig config = {0};
    v2styxlib_uart_configure_proto(&config, true, true, false);
    uint8_t payload[] = {0x11, 0x22};
    const uint16_t crc = v2styxlib_crc16_calculate(
        payload,
        static_cast<BufferSize_t>(sizeof(payload)));

    g_uartBytes.clear();
    v2styxlib_uart_send(&config, payload, static_cast<BufferSize_t>(sizeof(payload)));

    const std::vector<uint8_t> expected = {
        V2STYXLIB_SOF_MARKER_1,
        V2STYXLIB_SOF_MARKER_2,
        0x04,
        static_cast<uint8_t>((crc >> 8) & 0xFF),
        static_cast<uint8_t>(crc & 0xFF),
        0x11,
        0x22
    };
    REQUIRE(g_uartBytes == expected);
}
