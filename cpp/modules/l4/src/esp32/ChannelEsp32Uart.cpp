#include "esp32/ChannelEsp32Uart.h"
#include "driver/uart.h"
#include "esp_log.h"

namespace styxlib
{

ChannelEsp32Uart::ChannelEsp32Uart(
    const ChannelUartConfig *config,
    uart_port_t uartPortNum,
    int txPin,
    int rxPin,
    uint16_t rxBufferSize) : ChannelUart(config),
                                _uartPortNum(uartPortNum),
                                _txPin(txPin), 
                                _rxPin(rxPin),
                                _rxBufferSize(rxBufferSize)
{
}

ChannelEsp32Uart::~ChannelEsp32Uart()
{
    uart_driver_delete(_uartPortNum);
}

ErrorCode ChannelEsp32Uart::configureUart()
{
    int8_t result = v2styxlib_uart_esp32_setup(
        static_cast<const V2styxlibUartEsp32vConfig*>(_config)
    );
    switch (result)
    {
    case V2STYXLIB_ESP32_ERROR_NONE:
        return ErrorCode::None;
        break;
    case V2STYXLIB_ESP32_ERROR_NULLPTR_ARGUMENT:
        return ErrorCode::NullptrArgument;
        break;
    case V2STYXLIB_ESP32_ERROR_CONFIGURE_FAILED:
        return ErrorCode::ConfigureFailed;
        break;    
    default:
        return ErrorCode::Unknown;
        break;
    }
}

SizeResult ChannelEsp32Uart::internalSendBytes(
    const StyxBuffer buffer, 
    Size size
) {
    int err = v2styxlib_uart_esp32_send_bytes(
        static_cast<const V2styxlibUartEsp32vConfig*>(_config), 
        buffer, 
        size
    );
    if (err < 0) {
        return Unexpected(ErrorCode::SendFailed);
    }
    return SizeResult(static_cast<Size>(err));
}

}