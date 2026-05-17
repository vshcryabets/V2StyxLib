#ifndef ARCH_H
#define ARCH_H

#include "structs.h"

#ifdef TARGET_STM8
    #include "ChannelUartStm8.h"
    typedef V2styxlibUartStm8Config V2styxlibUartConfig;
    #define v2styxlib_uart_send_byte v2styxlib_uart_stm8_send_byte
#else
    // target to host system, use generic config
    typedef struct {
        struct V2styxlibUartConfigBase baseConfig; 
    } V2styxlibUartConfig;

    void v2styxlib_uart_send_byte(
        const V2styxlibUartConfig* config,
        uint8_t byte
    );
#endif

#endif // ARCH_H