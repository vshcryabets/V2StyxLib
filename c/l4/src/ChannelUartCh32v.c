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

#include "ChannelUartCh32v.h"

int8_t v2styxlib_uart_ch32v_setup(
    const V2styxlibUartCh32vConfig* config
) {
    uint32_t reg;
    uint32_t baud = config->baudrate;

    /* Enable clocks for GPIOD and USART1 */
    RCC->APB2PCENR |= (RCC_APB2Periph_GPIOD | RCC_APB2Periph_USART1);

    /* PD5: AF push-pull output, 50 MHz (MODE=11, CNF=10 => 0xB) */
    reg = GPIOD->CFGLR;
    reg &= ~(0xFu << (5u * 4u));
    reg |=  (0xBu << (5u * 4u));

    /* PD6: floating input (MODE=00, CNF=01 => 0x4) */
    reg &= ~(0xFu << (6u * 4u));
    reg |=  (0x4u << (6u * 4u));
    GPIOD->CFGLR = reg;

    if (baud == 0u) {
        baud = 9600u;
    }

    /* Configure baud rate for 16x oversampling */
    USART1->BRR = (uint16_t)((SystemCoreClock + (baud / 2u)) / baud);

    /* 8 data bits, no parity, 1 stop bit, no HW flow control */
    USART1->CTLR1 = (USART_CTLR1_TE | USART_CTLR1_RE);
    USART1->CTLR2 &= ~(USART_CTLR2_STOP);
    USART1->CTLR3 &= ~(USART_CTLR3_RTSE | USART_CTLR3_CTSE);

    /* Enable USART */
    USART1->CTLR1 |= USART_CTLR1_UE;
    return 0;
}

BufferSize_t v2styxlib_uart_ch32v_send_bytes(
    const V2styxlibUartCh32vConfig* config,
    const uint8_t* buffer,
    BufferSize_t length)
{
    (void)config;
    for (BufferSize_t i = 0; i < length; ++i) {
        while ((USART1->STATR & USART_STATR_TXE) == 0u);
        USART1->DATAR = buffer[i];
    }
    // wait for transmission complete before returning to ensure all bytes are sent
    while ((USART1->STATR & USART_STATR_TC) == 0u);
    return length;
}

