#include "arch.h"

#ifdef V2STYXLIB_TARGET_STM8
#elif defined(TARGET_CATCH2)
#else
#include <stdio.h>

void v2styxlib_uart_send_byte(
    const V2styxlibUartConfig* config,
    uint8_t byte
) {
    // For host system, we can just print the data to console for testing purposes
    printf("Sending data over UART: %02X\n", byte);
}
#endif