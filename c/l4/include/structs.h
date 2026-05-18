#ifndef STRUCTS_H
#define STRUCTS_H

#include "defines.h"

/**
 * V2styxlibUartConfigBase - is a structure that holds the 
 * configuration for UART communication. And hardware layer 
 * specific send function.
 */
typedef struct V2styxlibUartConfigBase {
    uint8_t config;
    uint8_t sof[2];
} V2styxlibUartConfigBase;

#endif // STRUCTS_H