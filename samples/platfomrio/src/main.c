#include "stm8s.h"
#include "stdbool.h"
#include "string.h"

#include "Channel_c.h"
#include "ChannelUartStm8.h"

#define SOFT_TX_PORT GPIOA
#define SOFT_TX_PIN  GPIO_PIN_2

typedef struct {
    uint8_t  msg_type;    // 0x01
    uint8_t  someByte;
    uint8_t message[16];
} StyxTestMessage;

void clock_setup(void) {
    CLK->CKDIVR = 0x00; // 16MHz HSI
}

void main(void) {
    clock_setup();
    V2styxlibUartConfig uartConfig;
    uartConfig.baseConfig.config = 0;
#ifdef V2STYXLIB_SOFTUART_TX
    uartConfig.softUartPort = SOFT_TX_PORT;
    uartConfig.softUartTxPinMask = SOFT_TX_PIN;
    v2styxlib_uart_configure_proto((V2styxlibUartConfig*)&uartConfig, true, true, true);
#else    
    v2styxlib_uart_configure_proto((V2styxlibUartConfig*)&uartConfig, true, true, false);
#endif    
    //v2styxlib_uart_setup(139); // 16Mhz/115200 ≈ 139
    v2styxlib_uart_stm8_setup(&uartConfig, 1666); // 16Mhz/9600

    StyxTestMessage pkt;
    pkt.msg_type = 0x01; // Gamepad state packet
    memcpy(pkt.message, "Hello, Styx!", sizeof("Hello, Styx!"));
    uint8_t counter = 0;

    while (1) {
        pkt.someByte = counter++;
        v2styxlib_uart_send(&uartConfig, (uint8_t*)&pkt, sizeof(pkt));
        /* ~500 ms delay at 16 MHz */
        for (uint32_t i = 0; i < 80000UL; i++);
    }
}