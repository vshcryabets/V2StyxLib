#ifndef STRUCTS_H
#define STRUCTS_H

#include <stdint.h>

/**
 * V2styxlibUartConfig - is a structure that holds the 
 * configuration for UART communication. And hardware layer 
 * specific send function.
 */
struct V2styxlibUartConfigBase {
    uint8_t config;
};

#endif // STRUCTS_H