#pragma once

#include "driver/uart.h"
#include "driver/gpio.h"

#include "uart/ChannelUart.h"


namespace styxlib
{

class ChannelEsp32Uart: public ChannelUart
{
private:
    uart_port_t _uartPortNum;
    int _txPin;
    int _rxPin;
protected:
    SizeResult internalSendBytes(
        const StyxBuffer buffer, 
        Size size) override;
public:
    ChannelEsp32Uart(
        const ChannelUartConfig* config,
        uart_port_t uartPortNum = UART_NUM_0,
        int txPin = UART_PIN_NO_CHANGE,
        int rxPin = UART_PIN_NO_CHANGE
    );
    virtual ~ChannelEsp32Uart();
    ErrorCode configureUart() override;
};

}