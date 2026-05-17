#ifndef ARCH_H
#define ARCH_H

#include "structs.h"

#ifdef V2STYXLIB_TARGET_STM8
    #include "ChannelUartStm8.h"

    typedef V2styxlibUartStm8Config V2styxlibUartConfig;
    #define v2styxlib_uart_send_bytes v2styxlib_uart_stm8_send_bytes
#elif defined(V2STYXLIB_TARGET_CH32V)
    #include "ChannelUartCh32v.h"

    typedef V2styxlibUartCh32vConfig V2styxlibUartConfig;
    #define v2styxlib_uart_send_bytes v2styxlib_uart_ch32v_send_bytes
#else
    // target to host system, use generic config
    typedef struct {
        struct V2styxlibUartConfigBase baseConfig; 
    } V2styxlibUartConfig;

    void v2styxlib_uart_send_bytes(
        const V2styxlibUartConfig* config,
        const uint8_t* buffer,
        BufferSize_t length
    );
#endif

#endif // ARCH_H