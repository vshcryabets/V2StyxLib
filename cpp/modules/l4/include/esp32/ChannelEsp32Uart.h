#pragma once

#include "uart/ChannelUart.h"

namespace styxlib
{

class ChannelEsp32Uart: public ChannelUart
{
protected:
    SizeResult internalSendBytes(
        const StyxBuffer buffer, 
        Size size) override;
public:
    ChannelEsp32Uart(
        const ChannelUartConfig* config,
        uart_port_t uartPortNum = UART_NUM_0,
        int txPin = UART_PIN_NO_CHANGE,
        int rxPin = UART_PIN_NO_CHANGE,
        uint16_t rxBufferSize = 256
    );
    virtual ~ChannelEsp32Uart();
    ErrorCode configureUart() override;
};

}