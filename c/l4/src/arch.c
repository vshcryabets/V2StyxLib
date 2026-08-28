#include "arch.h"

#ifdef V2STYXLIB_TARGET_STM8
#elif defined(TARGET_CATCH2)
#else
#include <stdio.h>

BufferSize_t v2styxlib_uart_send_bytes(
    const V2styxlibUartConfig* config,
    const uint8_t* buffer,
    BufferSize_t length
) {
    (void)config;
    // For host system, print bytes for testing/debug purposes.
    for (BufferSize_t i = 0; i < length; i++) {
        printf("Sending data over UART: %02X\n", buffer[i]);
    }
    return length;
}
#endif