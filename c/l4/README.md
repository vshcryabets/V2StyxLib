# Channel.h

UART communication channel configuration and CRC calculation definitions. You can redefine them.

## `BufferSize_t`
Type definition for buffer size. Defaults to `uint8_t` if not previously defined.  
Used to specify the maximum buffer size for UART data transmission.

## `V2STYXLIB_CRC16_POLY`
CRC16 polynomial value used for checksum calculation.  
Default: `0x1021` (CCITT polynomial).  
Used in the CRC16-CCITT algorithm for error detection.

## `V2STYXLIB_CRC16_INITIAL_VALUE`
Initial value for CRC16 calculation.  
Default: `0xFFFF`.  
Used as the starting point for CRC16 calculation.

## `V2STYXLIB_STREAMING_MODE`
Flag bit indicating streaming mode is enabled.  
Default: `0x01`.  
When set, UART operates in streaming mode:
- SOF (Start of Frame) markers are sent before each data frame.
- The sender does not wait for responses between transmissions.

## `V2STYXLIB_SOF_MARKER_1`
First byte of the Start of Frame (SOF) marker sequence.  
Default: `0x55`.  
Used in streaming mode to indicate the beginning of a new data frame.

## `V2STYXLIB_SOF_MARKER_2`
Second byte of the Start of Frame (SOF) marker sequence.  
Default: `0xAA`.  
Used with `V2STYXLIB_SOF_MARKER_1` to form the two-byte SOF marker sequence: `0x55 0xAA`.
