#ifndef DEFINES_H
#define DEFINES_H

#ifdef V2STYXLIB_TARGET_STM8
    #include "stm8s.h"
#else
    #include <stdint.h>
    #include <stdbool.h>
#endif

#ifndef BufferSize_t
    #define BufferSize_t uint8_t
#endif

#ifndef V2STYXLIB_CRC16_POLY
    #define V2STYXLIB_CRC16_POLY 0x1021
#endif
#ifndef V2STYXLIB_CRC16_INITIAL_VALUE
    #define V2STYXLIB_CRC16_INITIAL_VALUE 0xFFFF
#endif

#define V2STYXLIB_CONFIG_STREAMING_MODE 0x01
#define V2STYXLIB_CONFIG_SEND_CRC16 0x02
#define V2STYXLIB_CONFIG_SOFT_UART_TX 0x04

#ifndef V2STYXLIB_SOF_MARKER_1
    #define V2STYXLIB_SOF_MARKER_1 0x55
    #define V2STYXLIB_SOF_MARKER_2 0xAA
#endif

#endif // DEFINES_H