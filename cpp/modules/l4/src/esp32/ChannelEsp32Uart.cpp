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
    if (config == nullptr) {
         ESP_LOGE("ChannelEsp32Uart", "Failed to configure UART: config is null");
         return ErrorCode::NullptrArgument;
    }
    uart_config_t uart_config = {
        .baud_rate  = static_cast<int>(config->baudrate),
        .data_bits  = UART_DATA_8_BITS,
        .parity     = UART_PARITY_DISABLE,
        .stop_bits  = UART_STOP_BITS_1,
        .flow_ctrl  = UART_HW_FLOWCTRL_DISABLE,
        .rx_flow_ctrl_thresh = 0, // no flow control, so this value is ignored
        .source_clk = UART_SCLK_DEFAULT,
        .flags = {
            .allow_pd = 0,
            .backup_before_sleep = 0
        }
    };
    esp_err_t err = uart_param_config(_uartPortNum, &uart_config);
    if (err != ESP_OK) {
        ESP_LOGE("ChannelEsp32Uart", "Failed to configure UART parameters: %s", esp_err_to_name(err));
        return ErrorCode::ConfigureFailed;
    }

    err = uart_set_pin(_uartPortNum, _txPin, _rxPin, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    if (err != ESP_OK) {
        ESP_LOGE("ChannelEsp32Uart", "Failed to set UART pins: %s", esp_err_to_name(err));
        return ErrorCode::ConfigureFailed;
    }

    err = uart_driver_install(_uartPortNum, _rxBufferSize, 0, 0, NULL, 0);
    if (err != ESP_OK) {
        ESP_LOGE("ChannelEsp32Uart", "Failed to install UART driver: %s", esp_err_to_name(err));
        return ErrorCode::ConfigureFailed;
    }
    return ErrorCode::Success;
}

SizeResult ChannelEsp32Uart::internalSendBytes(
    const StyxBuffer buffer, 
    Size size
) {
    int err = uart_write_bytes(_uartPortNum, reinterpret_cast<const char*>(buffer), size);
    if (err < 0) {
        return Unexpected(ErrorCode::SendFailed);
    }
    return SizeResult(static_cast<Size>(err));
}

}