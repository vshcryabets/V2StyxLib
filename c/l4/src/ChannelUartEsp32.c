#if defined(CH32V00X)
#include <ch32v00x.h>
#elif defined(CH32V10X)
#include <ch32v10x.h>
#elif defined(CH32V20X)
#include <ch32v20x.h>
#elif defined(CH32V30X) || defined(CH32V31X)
#include <ch32v30x.h>
#elif defined(CH32L10X)
#include <ch32l103.h>
#endif

#include "ChannelUartEsp32.h"

int8_t v2styxlib_uart_esp32v_setup(
    const V2styxlibUartEsp32vConfig* config
) {
    if (config == nullptr) {
         ESP_LOGE("ChannelEsp32Uart", "Failed to configure UART: config is null");
         return V2STYXLIB_ESP32_ERROR_NULLPTR_ARGUMENT;
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
        return V2STYXLIB_ESP32_ERROR_CONFIGURE_FAILED;
    }

    err = uart_set_pin(_uartPortNum, _txPin, _rxPin, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    if (err != ESP_OK) {
        ESP_LOGE("ChannelEsp32Uart", "Failed to set UART pins: %s", esp_err_to_name(err));
        return V2STYXLIB_ESP32_ERROR_CONFIGURE_FAILED;
    }

    err = uart_driver_install(_uartPortNum, _rxBufferSize, 0, 0, NULL, 0);
    if (err != ESP_OK) {
        ESP_LOGE("ChannelEsp32Uart", "Failed to install UART driver: %s", esp_err_to_name(err));
        return V2STYXLIB_ESP32_ERROR_CONFIGURE_FAILED;
    }
    return V2STYXLIB_ESP32_ERROR_NONE;
}

BufferSize_t v2styxlib_uart_esp32_send_bytes(
    const V2styxlibUartEsp32vConfig* config,
    const uint8_t* buffer,
    BufferSize_t length)
{
    return uart_write_bytes(
        config->_uartPortNum, 
        reinterpret_cast<const char*>(buffer), 
        length
    );
}

